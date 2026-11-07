package cr.ac.una.eif206.frontend.reportes;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.swing.JTable;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Genera reportes en PDF (libreria OpenPDF) a partir de una JTable.
 * El enunciado pide PDF en TODAS las pantallas, asi que todas usan estos metodos.
 *
 * Los archivos se guardan en la carpeta "reportes" (se crea sola) y el metodo
 * devuelve la ruta completa para mostrarsela al usuario.
 */
public final class ReportePdfGenerator {

    private static final String CARPETA_REPORTES = "reportes";
    private static final DateTimeFormatter FORMATO_NOMBRE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportePdfGenerator() {
    }

    /**
     * PDF con el titulo y una tabla con los mismos encabezados y datos de la JTable.
     * Hoja A4 horizontal, porque Calendarizacion y Actividades tienen muchas columnas.
     * Las celdas con varias lineas (varias actividades) se respetan.
     */
    public static String generarReporteTabla(String tituloReporte, JTable tablaOrigen) throws Exception {
        if (tablaOrigen == null) {
            throw new IllegalArgumentException("La tabla utilizada para generar el reporte no puede ser null.");
        }
        String ruta = rutaNueva(tituloReporte);

        Document documento = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(documento, new FileOutputStream(ruta));
        documento.open();
        try {
            agregarTitulo(documento, tituloReporte);
            documento.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(FORMATO_FECHA_HORA)));
            documento.add(new Paragraph(" "));
            agregarTabla(documento, tablaOrigen);
        } finally {
            documento.close();
        }
        return new File(ruta).getAbsolutePath();
    }

    /** Igual que generarReporteTabla, pero indicando el periodo consultado (desde/hasta ya formateados). */
    public static String generarReporteEstadisticas(String titulo, String desde, String hasta,
                                                    JTable tabla) throws Exception {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla utilizada para generar el reporte no puede ser null.");
        }
        String ruta = rutaNueva(titulo);

        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, new FileOutputStream(ruta));
        documento.open();
        try {
            agregarTitulo(documento, titulo);
            documento.add(new Paragraph("Período consultado: " + desde + " hasta " + hasta));
            documento.add(new Paragraph(" "));
            agregarTabla(documento, tabla);
        } finally {
            documento.close();
        }
        return new File(ruta).getAbsolutePath();
    }

    // ------------------------------------------------------------------

    /** Crea la carpeta si hace falta y arma un nombre unico: titulo + fecha y hora. */
    private static String rutaNueva(String titulo) {
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        String base = titulo.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        return CARPETA_REPORTES + File.separator + base + "_" + LocalDateTime.now().format(FORMATO_NOMBRE) + ".pdf";
    }

    private static void agregarTitulo(Document documento, String titulo) throws Exception {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph parrafo = new Paragraph(titulo, fuente);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafo);
        documento.add(new Paragraph(" "));
    }

    private static void agregarTabla(Document documento, JTable origen) throws Exception {
        int columnas = origen.getColumnCount();
        if (columnas == 0) {
            documento.add(new Paragraph("No existen columnas para mostrar."));
            return;
        }

        PdfPTable tablaPdf = new PdfPTable(columnas);
        tablaPdf.setWidthPercentage(100);

        for (int c = 0; c < columnas; c++) {
            PdfPCell encabezado = new PdfPCell(new Paragraph(origen.getColumnName(c),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            encabezado.setBackgroundColor(new Color(220, 220, 220));
            tablaPdf.addCell(encabezado);
        }
        tablaPdf.setHeaderRows(1);   // el encabezado se repite en cada pagina

        if (origen.getRowCount() == 0) {
            PdfPCell sinDatos = new PdfPCell(new Paragraph("No existen datos para mostrar."));
            sinDatos.setColspan(columnas);
            sinDatos.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaPdf.addCell(sinDatos);
        } else {
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (int f = 0; f < origen.getRowCount(); f++) {
                for (int c = 0; c < columnas; c++) {
                    Object valor = origen.getValueAt(f, c);
                    tablaPdf.addCell(new Paragraph(valor == null ? "" : valor.toString(), fuenteCelda));
                }
            }
        }
        documento.add(tablaPdf);
    }
}
