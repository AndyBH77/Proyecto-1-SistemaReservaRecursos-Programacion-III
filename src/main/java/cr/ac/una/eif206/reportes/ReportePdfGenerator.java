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
}
