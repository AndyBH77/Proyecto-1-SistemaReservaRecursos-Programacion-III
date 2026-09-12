package cr.ac.una.eif206.reportes;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import cr.ac.una.eif206.modelo.Reserva;

import javax.swing.*;
import javax.swing.JTable;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera reportes en PDF usando la librería OpenPDF (una version libre y
 * mantenida de iText). Este generador se puede reutilizar/ampliar para las
 * demas pantallas (Funcionarios, Categorias, etc.) que hagan los otros
 * integrantes del equipo, ya que el enunciado pide reporte en PDF en TODAS
 * las funcionalidades.
 */
public class ReportePdfGenerator {

    private static final String CARPETA_REPORTES = "reportes";

    public static String generarReporteReservas(String idFuncionario, List<Reserva> reservas) throws Exception {
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        String nombreArchivo = "reservas_" + idFuncionario + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        String rutaCompleta = CARPETA_REPORTES + File.separator + nombreArchivo;

        Document documento = new Document();
        PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
        documento.open();

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("Listado de Reservas - Funcionario " + idFuncionario, fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        documento.add(new Paragraph(" ")); // espacio en blanco

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);

        agregarEncabezado(tabla, "Id");
        agregarEncabezado(tabla, "Actividad");
        agregarEncabezado(tabla, "Fecha");
        agregarEncabezado(tabla, "Hora Inicio");
        agregarEncabezado(tabla, "Hora Fin");
        agregarEncabezado(tabla, "Estado");

        for (Reserva r : reservas) {
            tabla.addCell(r.getId());
            tabla.addCell(r.getActividad());
            tabla.addCell(r.getFecha());
            tabla.addCell(r.getHoraInicio());
            tabla.addCell(r.getHoraFin());
            tabla.addCell(r.getEstado().toString());
        }

        documento.add(tabla);
        documento.close();

        return new File(rutaCompleta).getAbsolutePath();
    }

    private static void agregarEncabezado(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        celda.setBackgroundColor(new Color(220, 220, 220));
        tabla.addCell(celda);
    }

    /**
     * Genera un archivo PDF utilizando los datos contenidos en una JTable.
     * <p>
     * Este método se puede reutilizar para imprimir:
     * - Calendarización de recursos.
     * - Programación de actividades.
     * - Estadísticas.
     *
     * @param tituloReporte título que aparecerá en la parte superior del PDF.
     * @param tablaOrigen   JTable de donde se copiarán los encabezados y los datos.
     * @return ruta absoluta donde se guardó el archivo PDF.
     * @throws Exception si ocurre un error al crear el archivo.
     */
    public static String generarReporteTabla(String tituloReporte, JTable tablaOrigen
    ) throws Exception {
        //Crear la carpeta "reportes" si todavía no existe.
        File carpeta = new File(CARPETA_REPORTES);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        if (tablaOrigen == null) { //Si se recibe null, no se puede generar el reporte porque no existiría una tabla de donde obtener la información
            throw new IllegalArgumentException("La tabla utilizada para generar el reporte no puede ser null.");
        }
        String nombreBase = tituloReporte.toLowerCase().replaceAll("[^a-z0-9]+", "_"); //Convertir el título en un texto que pueda utilizarse nombre de archivo.
        String nombreArchivo = nombreBase + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf"; // La fecha y hora evitan que un reporte nuevo sobrescriba otro reporte generado anteriormente.
        String rutaCompleta = CARPETA_REPORTES + File.separator + nombreArchivo;//Construir la ruta completa donde se guardará el archivo.

        Document documento = new Document(com.lowagie.text.PageSize.A4.rotate());//Crear el documento PDF. Se utiliza A4 horizontal porque las tablas de Calendarización y Actividades pueden tener muchas columnas.
        PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));//Conectar el documento con el archivo físico que se creará.
        documento.open();
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);//Crear la fuente que tendrá el título.
        Paragraph titulo = new Paragraph(tituloReporte, fuenteTitulo);//Crear el título principal del reporte.
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo); // Agregar el título y un espacio en blanco al documento.
        documento.add(new Paragraph(" "));
        String fechaGeneracion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));//Agregar la fecha y hora en que se generó el reporte.
        documento.add(new Paragraph("Fecha de generación: " + fechaGeneracion));
        documento.add(new Paragraph(" "));
        int cantidadColumnas = tablaOrigen.getColumnCount(); //Obtener la cantidad de columnas que tiene la JTable.
        if (cantidadColumnas == 0) { //Verificar que la tabla tenga columnas.
            documento.add(new Paragraph("No existen columnas para mostrar."));
            documento.close();
            return new File(rutaCompleta).getAbsolutePath();
        }
        PdfPTable tablaPdf = new PdfPTable(cantidadColumnas); //Crear la tabla que será colocada dentro del PDF. Tendrá exactamente la misma cantidad de columnas que la JTable de la interfaz gráfica.
        tablaPdf.setWidthPercentage(100);
        for (int columna = 0; columna < cantidadColumnas; columna++) { //Copiar los encabezados de la JTable. getColumnName(columna) devuelve el nombre de cada columna.
            String nombreColumna = tablaOrigen.getColumnName(columna);
            agregarEncabezado(tablaPdf, nombreColumna);
        }
        if (tablaOrigen.getRowCount() == 0) { //Verificar si la tabla tiene filas. Si no tiene, el PDF seguirá generándose, pero mostrará un mensaje indicando que no existen datos.
            PdfPCell celdaSinDatos = new PdfPCell(new Paragraph("No existen datos para mostrar."));
            celdaSinDatos.setColspan(cantidadColumnas);
            celdaSinDatos.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaPdf.addCell(celdaSinDatos);
        } else {
            for (int fila = 0; fila < tablaOrigen.getRowCount(); fila++) {
                for (int columna = 0; columna < cantidadColumnas; columna++) {
                    Object valor = tablaOrigen.getValueAt(fila, columna);
                    String textoCelda;
                    if (valor == null) {
                        textoCelda = "";
                    } else {
                        textoCelda = valor.toString();
                    }
                    tablaPdf.addCell(textoCelda);
                }
            }
        }
        documento.add(tablaPdf);
        documento.close();
        return new File(rutaCompleta).getAbsolutePath(); //Devolver la ruta absoluta del archivo creado. El controlador usa esta ruta para mostrarle al usuario dónde quedó guardado el reporte.
    }

    public static String generarReporteEstadisticas(String titulo, String desde, String hasta, JTable tabla) throws Exception {
        //Crear la carpeta "reportes" si todavía no existe.
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        String nombreArchivo = "estadisticas_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        String rutaCompleta = CARPETA_REPORTES + File.separator + nombreArchivo;

        Document documento = new Document();
        PdfWriter.getInstance(documento, new FileOutputStream(rutaCompleta));
        documento.open();
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18); // Crea y agrega el título.
        Paragraph parrafoTitulo = new Paragraph(titulo, fuenteTitulo);
        parrafoTitulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoTitulo);
        documento.add(new Paragraph(" "));
        documento.add(new Paragraph("Período consultado: " + desde + " hasta " + hasta)); // Agrega el período consultado.
        documento.add(new Paragraph(" "));
        int cantidadColumnas = tabla.getColumnCount();  // Obtiene la cantidad de columnas de la JTable.
        if (cantidadColumnas == 0) { // Si la tabla no tiene columnas, muestra un mensaje.
            documento.add(new Paragraph("No existen datos para mostrar."));
            documento.close();
            return new File(rutaCompleta).getAbsolutePath();
        }
        PdfPTable tablaPdf = new PdfPTable(cantidadColumnas); // Crea la tabla que será colocada en el PDF.
        tablaPdf.setWidthPercentage(100);
        for (int columna = 0; columna < cantidadColumnas; columna++) { // Crea directamente los encabezados.
            String nombreColumna = tabla.getColumnName(columna);
            Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Paragraph textoEncabezado = new Paragraph(nombreColumna, fuenteEncabezado);
            PdfPCell celdaEncabezado = new PdfPCell(textoEncabezado);
            celdaEncabezado.setBackgroundColor(new Color(220, 220, 220));
            tablaPdf.addCell(celdaEncabezado);
        }
        for (int fila = 0; fila < tabla.getRowCount(); fila++) { // Copia los datos de la JTable.
            for (int columna = 0; columna < cantidadColumnas; columna++) {
                Object valor = tabla.getValueAt(fila, columna
                );
                if (valor == null) {
                    tablaPdf.addCell("");
                } else {
                    tablaPdf.addCell(valor.toString());
                }
            }
        }
        documento.add(tablaPdf);
        documento.close();
        return new File(rutaCompleta).getAbsolutePath(); // Devuelve la ruta absoluta del archivo.
    }
}