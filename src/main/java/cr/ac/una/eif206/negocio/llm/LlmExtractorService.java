package cr.ac.una.eif206.negocio.llm;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.util.ConfigManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio encargado de "entender" la frase en lenguaje natural que escribe
 * el funcionario y extraer de ahi los datos de la reserva (actividad, fecha,
 * horas y categorias de recurso).
 *
 * Funciona en dos modos:
 *   1) Si hay una llave de OpenAI configurada en config.properties, se le
 *      pregunta a un modelo de lenguaje (LLM) real.
 *   2) Si no hay llave configurada (o la llamada falla, por ejemplo por no
 *      tener internet), se usa un extractor simple basado en palabras clave
 *      y expresiones regulares, para que la funcionalidad igual funcione
 *      sin depender de un servicio externo de pago.
 */
public class LlmExtractorService {

    public DatosReservaExtraidos extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) {
        String apiKey = ConfigManager.obtener("openai.apiKey");

        if (!apiKey.isBlank()) {
            try {
                return extraerConOpenAI(frase, categoriasDisponibles, apiKey);
            } catch (Exception e) {
                // Si la llamada a la IA falla (sin internet, llave invalida, etc.)
                // se cae al extractor simple para que la funcionalidad no se caiga por completo.
                System.err.println("Fallo la llamada a la IA, se usa el extractor simple. Detalle: " + e.getMessage());
            }
        }

        return extraerSinIA(frase, categoriasDisponibles);
    }

    // ------------------------------------------------------------------
    // Opcion 1: extractor simple por palabras clave (siempre disponible,
    // no necesita conexion a internet ni api key)
    // ------------------------------------------------------------------

    private DatosReservaExtraidos extraerSinIA(String frase, List<CategoriaRecurso> categoriasDisponibles) {
        DatosReservaExtraidos datos = new DatosReservaExtraidos();
        String textoMinusculas = frase.toLowerCase(Locale.forLanguageTag("es"));

        datos.fecha = buscarFecha(textoMinusculas);
        datos.horaInicio = buscarHora(textoMinusculas, true);
        datos.horaFin = buscarHora(textoMinusculas, false);

        for (CategoriaRecurso categoria : categoriasDisponibles) {
            if (textoMinusculas.contains(categoria.getDescripcion().toLowerCase(Locale.forLanguageTag("es")))) {
                datos.nombresCategorias.add(categoria.getDescripcion());
            }
        }

        // Como actividad, a falta de algo mas elaborado, se usa la frase completa
        // (el usuario despues puede corregirla a mano en el formulario).
        datos.actividad = frase.length() > 60 ? frase.substring(0, 60) : frase;

        return datos;
    }

    private LocalDate buscarFecha(String texto) {
        // Busca patrones tipo "14 de agosto" (se asume el anio actual)
        Pattern patron = Pattern.compile(
                "(\\d{1,2})\\s+de\\s+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|"
                        + "setiembre|septiembre|octubre|noviembre|diciembre)");
        Matcher matcher = patron.matcher(texto);

        if (matcher.find()) {
            int dia = Integer.parseInt(matcher.group(1));
            int mes = numeroDeMes(matcher.group(2));
            try {
                return LocalDate.of(LocalDate.now().getYear(), mes, dia);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private int numeroDeMes(String nombreMes) {
        return switch (nombreMes) {
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

    private LocalTime buscarHora(String texto, boolean esInicio) {
        // Busca patrones tipo "8am a 10am", "8 a.m. a 10 a.m.", "14 a 16"
        Pattern patronRango = Pattern.compile(
                "(\\d{1,2})\\s*(am|pm|a\\.?\\s?m\\.?|p\\.?\\s?m\\.?)?\\s*(?:a|hasta|-)\\s*"
                        + "(\\d{1,2})\\s*(am|pm|a\\.?\\s?m\\.?|p\\.?\\s?m\\.?)?");
        Matcher matcher = patronRango.matcher(texto);

        if (!matcher.find()) {
            return null;
        }

        int horaInicio = ajustarHora(Integer.parseInt(matcher.group(1)), matcher.group(2));
        int horaFin = ajustarHora(Integer.parseInt(matcher.group(3)), matcher.group(4));

        return LocalTime.of(esInicio ? horaInicio : horaFin, 0);
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
            return 0; // 12am = medianoche
        }
        return hora;
    }

    // ------------------------------------------------------------------
    // Opcion 2: llamada real a un modelo de lenguaje (API de OpenAI)
    // ------------------------------------------------------------------

    private DatosReservaExtraidos extraerConOpenAI(String frase, List<CategoriaRecurso> categoriasDisponibles,
                                                   String apiKey) throws Exception {

        String modelo = ConfigManager.obtener("openai.modelo", "gpt-4o-mini");

        StringBuilder nombresCategorias = new StringBuilder();
        for (CategoriaRecurso c : categoriasDisponibles) {
            nombresCategorias.append("- ").append(c.getDescripcion()).append("\n");
        }

        String promptSistema = "Eres un asistente que extrae datos de una reserva de recursos a partir de una "
                + "frase en español. Responde UNICAMENTE con un JSON (sin texto adicional) con este formato:\n"
                + "{\n"
                + "  \"actividad\": \"...\",\n"
                + "  \"fecha\": \"yyyy-MM-dd\",\n"
                + "  \"horaInicio\": \"HH:mm\",\n"
                + "  \"horaFin\": \"HH:mm\",\n"
                + "  \"categorias\": [\"...\"]\n"
                + "}\n"
                + "El campo \"categorias\" solo puede contener nombres tomados EXACTAMENTE de esta lista:\n"
                + nombresCategorias
                + "El anio actual es " + LocalDate.now().getYear() + " si la frase no indica el anio.";

        JSONObject cuerpoRequest = new JSONObject();
        cuerpoRequest.put("model", modelo);

        JSONArray mensajes = new JSONArray();
        mensajes.put(new JSONObject().put("role", "system").put("content", promptSistema));
        mensajes.put(new JSONObject().put("role", "user").put("content", frase));
        cuerpoRequest.put("messages", mensajes);
        cuerpoRequest.put("temperature", 0.2);

        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(cuerpoRequest.toString()))
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new Exception("La API respondio con codigo " + respuesta.statusCode() + ": " + respuesta.body());
        }

        JSONObject json = new JSONObject(respuesta.body());
        String contenido = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // Por si el modelo agrega texto extra alrededor del JSON, se recorta
        // desde la primera "{" hasta la ultima "}".
        int inicio = contenido.indexOf('{');
        int fin = contenido.lastIndexOf('}');
        JSONObject datosJson = new JSONObject(contenido.substring(inicio, fin + 1));

        DatosReservaExtraidos datos = new DatosReservaExtraidos();
        datos.actividad = datosJson.optString("actividad", "");

        if (datosJson.has("fecha") && !datosJson.isNull("fecha")) {
            datos.fecha = LocalDate.parse(datosJson.getString("fecha"));
        }
        if (datosJson.has("horaInicio") && !datosJson.isNull("horaInicio")) {
            datos.horaInicio = LocalTime.parse(datosJson.getString("horaInicio"));
        }
        if (datosJson.has("horaFin") && !datosJson.isNull("horaFin")) {
            datos.horaFin = LocalTime.parse(datosJson.getString("horaFin"));
        }

        JSONArray categoriasJson = datosJson.optJSONArray("categorias");
        if (categoriasJson != null) {
            for (int i = 0; i < categoriasJson.length(); i++) {
                datos.nombresCategorias.add(categoriasJson.getString(i));
            }
        }

        return datos;
    }
}
