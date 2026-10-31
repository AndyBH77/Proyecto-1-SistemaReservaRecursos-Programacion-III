package cr.ac.una.eif206.backend.logica.llm;

import cr.ac.una.eif206.backend.util.ConfigManager;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.DatosReservaExtraidos;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae los datos de una reserva (actividad, fecha, horas, categorias)
 * a partir de una frase en lenguaje natural.
 *
 * Dos modos:
 *  1) Si hay gemini.apiKey en config.properties, se le pregunta a Google Gemini.
 *  2) Si no hay llave, o Gemini falla (sin internet, llave invalida...), se usa
 *     un extractor simple con expresiones regulares, para que nunca se caiga.
 *
 * Corre en el BACKEND: la llave de la IA nunca sale del servidor.
 */
public class LlmExtractorService {

    private static final String URL_GEMINI =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    public DatosReservaExtraidos extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) throws Exception {
        if (frase == null || frase.isBlank()) {
            throw new Exception("Escriba primero una frase describiendo la reserva.");
        }

        String apiKey = ConfigManager.obtener("gemini.apiKey");
        if (!apiKey.isEmpty()) {
            try {
                DatosReservaExtraidos datos = extraerConGemini(frase, categoriasDisponibles, apiKey);
                System.out.println("[IA] Datos extraidos con Gemini.");
                return datos;
            } catch (Exception e) {
                System.err.println("[IA] Fallo Gemini, se usa el extractor simple. Detalle: " + e.getMessage());
            }
        }

        System.out.println("[IA] Datos extraidos con el extractor simple (sin IA).");
        return extraerSinIA(frase, categoriasDisponibles);
    }

    // ==================================================================
    // Modo 1: Google Gemini
    // ==================================================================

    private DatosReservaExtraidos extraerConGemini(String frase, List<CategoriaRecurso> categoriasDisponibles,
                                                   String apiKey) throws Exception {
        String modelo = ConfigManager.obtener("gemini.modelo");
        if (modelo.isEmpty()) {
            modelo = "gemini-3.6-flash";
        }

        StringBuilder nombres = new StringBuilder();
        for (CategoriaRecurso c : categoriasDisponibles) {
            nombres.append("- ").append(c.getDescripcion()).append("\n");
        }

        String instrucciones = "Eres un asistente que extrae datos de una reserva de recursos a partir de una "
                + "frase en espanol. Responde UNICAMENTE con un JSON con este formato:\n"
                + "{\"actividad\": \"...\", \"fecha\": \"yyyy-MM-dd\", \"horaInicio\": \"HH:mm\", "
                + "\"horaFin\": \"HH:mm\", \"categorias\": [\"...\"]}\n"
                + "Si un dato no aparece en la frase, usa null.\n"
                + "\"categorias\" solo puede tener nombres tomados EXACTAMENTE de esta lista:\n"
                + nombres
                + "Hoy es " + LocalDate.now() + ". Si la frase no dice el anio, usa la proxima fecha "
                + "que corresponda (nunca una fecha pasada).";

        // Cuerpo de la peticion segun la API de Gemini (generateContent)
        JSONObject cuerpo = new JSONObject();
        cuerpo.put("systemInstruction", new JSONObject()
                .put("parts", new JSONArray().put(new JSONObject().put("text", instrucciones))));
        cuerpo.put("contents", new JSONArray().put(new JSONObject()
                .put("role", "user")
                .put("parts", new JSONArray().put(new JSONObject().put("text", frase)))));
        cuerpo.put("generationConfig", new JSONObject()
                .put("temperature", 0.2)
                .put("responseMimeType", "application/json"));

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(String.format(URL_GEMINI, modelo)))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString()))
                .build();

        HttpResponse<String> respuesta = HttpClient.newHttpClient()
                .send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new Exception("Gemini respondio con codigo " + respuesta.statusCode() + ": " + respuesta.body());
        }

        // La respuesta viene en candidates[0].content.parts[*].text
        JSONArray partes = new JSONObject(respuesta.body())
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts");

        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < partes.length(); i++) {
            JSONObject parte = partes.getJSONObject(i);
            if (!parte.optBoolean("thought", false) && parte.has("text")) {
                texto.append(parte.getString("text"));
            }
        }

        String contenido = texto.toString();
        int inicio = contenido.indexOf('{');
        int fin = contenido.lastIndexOf('}');
        if (inicio < 0 || fin < inicio) {
            throw new Exception("Gemini no devolvio un JSON valido: " + contenido);
        }
        JSONObject json = new JSONObject(contenido.substring(inicio, fin + 1));

        DatosReservaExtraidos datos = new DatosReservaExtraidos();
        datos.actividad = json.optString("actividad", null);
        datos.fecha = leerFecha(json.optString("fecha", ""));
        datos.horaInicio = leerHora(json.optString("horaInicio", ""));
        datos.horaFin = leerHora(json.optString("horaFin", ""));

        JSONArray categorias = json.optJSONArray("categorias");
        if (categorias != null) {
            for (int i = 0; i < categorias.length(); i++) {
                datos.nombresCategorias.add(categorias.getString(i));
            }
        }
        return datos;
    }

    private static LocalDate leerFecha(String texto) {
        try {
            return texto.isBlank() ? null : LocalDate.parse(texto);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime leerHora(String texto) {
        try {
            return texto.isBlank() ? null : LocalTime.parse(texto);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================================================================
    // Modo 2: extractor simple (sin internet, sin llave)
    // ==================================================================

    private DatosReservaExtraidos extraerSinIA(String frase, List<CategoriaRecurso> categoriasDisponibles) {
        DatosReservaExtraidos datos = new DatosReservaExtraidos();
        String texto = frase.toLowerCase(Locale.forLanguageTag("es"));

        datos.fecha = buscarFecha(texto);
        datos.horaInicio = buscarHora(texto, true);
        datos.horaFin = buscarHora(texto, false);

        for (CategoriaRecurso categoria : categoriasDisponibles) {
            if (texto.contains(categoria.getDescripcion().toLowerCase(Locale.forLanguageTag("es")))) {
                datos.nombresCategorias.add(categoria.getDescripcion());
            }
        }

        // Como actividad se usa la frase (recortada); el usuario la puede corregir.
        datos.actividad = frase.length() > 60 ? frase.substring(0, 60) : frase;
        return datos;
    }

    /** Busca "14 de agosto". Si esa fecha ya paso este anio, usa la del anio siguiente. */
    private LocalDate buscarFecha(String texto) {
        Pattern patron = Pattern.compile(
                "(\\d{1,2})\\s+de\\s+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|"
                        + "setiembre|septiembre|octubre|noviembre|diciembre)");
        Matcher matcher = patron.matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        try {
            LocalDate fecha = LocalDate.of(LocalDate.now().getYear(),
                    numeroDeMes(matcher.group(2)), Integer.parseInt(matcher.group(1)));
            return fecha.isBefore(LocalDate.now()) ? fecha.plusYears(1) : fecha;
        } catch (Exception e) {
            return null;
        }
    }

    private int numeroDeMes(String mes) {
        return switch (mes) {
            case "enero" -> 1;
            case "febrero" -> 2;
            case "marzo" -> 3;
            case "abril" -> 4;
            case "mayo" -> 5;
            case "junio" -> 6;
            case "julio" -> 7;
            case "agosto" -> 8;
            case "setiembre", "septiembre" -> 9;
            case "octubre" -> 10;
            case "noviembre" -> 11;
            case "diciembre" -> 12;
            default -> 1;
        };
    }

    /** Busca rangos tipo "8am a 10am", "2 pm a 4 pm", "14 a 16". */
    private LocalTime buscarHora(String texto, boolean esInicio) {
        Pattern patron = Pattern.compile(
                "(\\d{1,2})\\s*(am|pm|a\\.?\\s?m\\.?|p\\.?\\s?m\\.?)?\\s*(?:a|hasta|-)\\s*"
                        + "(\\d{1,2})\\s*(am|pm|a\\.?\\s?m\\.?|p\\.?\\s?m\\.?)?");
        Matcher matcher = patron.matcher(texto);
        if (!matcher.find()) {
            return null;
        }
        int inicio = ajustarHora(Integer.parseInt(matcher.group(1)), matcher.group(2));
        int fin = ajustarHora(Integer.parseInt(matcher.group(3)), matcher.group(4));
        try {
            return LocalTime.of(esInicio ? inicio : fin, 0);
        } catch (Exception e) {
            return null;
        }
    }

    private int ajustarHora(int hora, String sufijo) {
        if (sufijo == null) {
            return hora;
        }
        String s = sufijo.toLowerCase(Locale.ROOT);
        if (s.startsWith("p") && hora < 12) {
            return hora + 12;
        }
        if (s.startsWith("a") && hora == 12) {
            return 0;
        }
        return hora;
    }
}