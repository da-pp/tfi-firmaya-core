package com.firmaya.core.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ErrorInternoException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Arma el PDF del contrato con OpenPDF (CU-10 pasos 5 a 11 y 19).
 */
@Component
public class GeneradorPdf {

    static final String MENSAJE_ERROR = "El PDF no pudo ser generado en este momento.";
    static final String LEYENDA = "Documento con firma digital verificada por FirmaYA";

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font FUENTE_PIE = FontFactory.getFont(FontFactory.HELVETICA, 8);

    /**
     * @param firmas firmas completadas a incluir; null para la "versión actual sin firmas"
     */
    public byte[] generar(Contrato contrato, VersionContrato version, List<Firma> firmas) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 50, 50, 50, 60);
            PdfWriter.getInstance(documento, salida);

            // Paso 11: hash en el pie de página de cada página
            HeaderFooter pie = new HeaderFooter(
                    new Phrase("Hash del documento: " + version.getHashSha256(), FUENTE_PIE), false);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setBorder(Rectangle.NO_BORDER);
            documento.setFooter(pie);

            documento.open();

            // Paso 5: contenido del contrato en su versión final
            documento.add(new Paragraph(contrato.getNombre(), FUENTE_TITULO));
            documento.add(new Paragraph("Versión " + version.getNumeroVersion(), FUENTE_TEXTO));
            documento.add(new Paragraph(" ", FUENTE_TEXTO));
            for (String parrafo : ContenidoHtml.textoPlano(version.getContenido()).split("\n")) {
                documento.add(new Paragraph(parrafo, FUENTE_TEXTO));
            }

            if (firmas != null) {
                // Pasos 6 a 10: datos de firma por firmante
                documento.add(new Paragraph(" ", FUENTE_TEXTO));
                documento.add(new Paragraph("Datos de firma", FUENTE_SUBTITULO));
                for (Firma firma : firmas) {
                    documento.add(new Paragraph(" ", FUENTE_TEXTO));
                    documento.add(new Paragraph("Nombre del firmante: " + firma.getParte().getNombreParte(),
                            FUENTE_TEXTO));
                    documento.add(new Paragraph("Fecha y hora de la firma: "
                            + firma.getFechaFirma().format(FORMATO_FECHA_HORA), FUENTE_TEXTO));
                    documento.add(new Paragraph("Dirección IP del firmante: " + firma.getDireccionIp(), FUENTE_TEXTO));
                    documento.add(new Paragraph("Hash de la versión firmada: " + firma.getHashFirma(), FUENTE_TEXTO));
                }
                // Paso 19
                documento.add(new Paragraph(" ", FUENTE_TEXTO));
                documento.add(new Paragraph(LEYENDA, FUENTE_SUBTITULO));
            }

            documento.close();
            return salida.toByteArray();
        } catch (Exception ex) {
            // Camino alternativo "Error en la generación del PDF"
            throw new ErrorInternoException(MENSAJE_ERROR, ex);
        }
    }
}
