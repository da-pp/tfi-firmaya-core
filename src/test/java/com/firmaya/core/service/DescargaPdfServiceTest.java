package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.firmaya.core.dto.ArchivoPdf;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.DocumentoPdf;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.DocumentoPdfRepository;
import com.firmaya.core.repository.FirmaRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

class DescargaPdfServiceTest {

    private ContratoService contratoService;
    private FirmaRepository firmaRepository;
    private DocumentoPdfRepository documentoPdfRepository;
    private AuditoriaService auditoriaService;
    private DescargaPdfService service;
    private Usuario usuario;
    private Contrato contrato;
    private VersionContrato version;

    @BeforeEach
    void preparar() {
        contratoService = mock(ContratoService.class);
        firmaRepository = mock(FirmaRepository.class);
        documentoPdfRepository = mock(DocumentoPdfRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new DescargaPdfService(contratoService, mock(AccesoExternoService.class), firmaRepository,
                documentoPdfRepository, new GeneradorPdf(), auditoriaService);

        usuario = new Usuario();
        usuario.setNombre("María");
        usuario.setApellido("Gómez");

        EstadoContrato firmado = new EstadoContrato();
        firmado.setNombre(EstadoContrato.FIRMADO);
        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación Av. Corrientes");
        contrato.setEstado(firmado);
        when(contratoService.buscarContrato(10)).thenReturn(contrato);

        version = new VersionContrato();
        version.setIdVersion(7);
        version.setNumeroVersion(5);
        version.setContenido("<p>CONTRATO DE LOCACIÓN</p><p>El inmueble se ubica en Av. Corrientes 1240.</p>");
        version.setHashSha256(Sha256.calcular(version.getContenido()));
        when(contratoService.versionActual(10)).thenReturn(version);

        UsuarioContrato ana = new UsuarioContrato();
        ana.setNombreParte("Ana Martínez");
        Firma firma = new Firma();
        firma.setParte(ana);
        firma.setEstadoFirma(Firma.ESTADO_FIRMADO);
        firma.setFechaFirma(LocalDateTime.of(2026, 5, 15, 11, 31));
        firma.setDireccionIp("181.44.10.22");
        firma.setHashFirma(version.getHashSha256());
        when(firmaRepository.findByParteContratoIdContrato(10)).thenReturn(Arrays.asList(firma));
        when(documentoPdfRepository.findFirstByVersionIdVersionOrderByIdPdfDesc(7)).thenReturn(Optional.empty());
    }

    @Test
    void pdfFirmadoIncluyeDatosDeFirmaLeyendaYHashEnElPie() throws Exception {
        ArchivoPdf archivo = service.descargarFirmado(10, usuario, "10.0.0.1");

        assertEquals("Locación Av. Corrientes_v5_firmado.pdf", archivo.getNombreArchivo());
        String texto = textoDelPdf(archivo.getContenido());
        assertTrue(texto.contains("Av. Corrientes 1240"));
        assertTrue(texto.contains("Nombre del firmante: Ana Martínez"));
        assertTrue(texto.contains("Fecha y hora de la firma: 15/05/2026 11:31"));
        assertTrue(texto.contains("Dirección IP del firmante: 181.44.10.22"));
        assertTrue(texto.contains(GeneradorPdf.LEYENDA));
        assertTrue(texto.contains("Hash del documento: " + version.getHashSha256()));

        // Se guarda en DOCUMENTO_PDF con el hash del archivo y se audita la descarga
        ArgumentCaptor<DocumentoPdf> captor = ArgumentCaptor.forClass(DocumentoPdf.class);
        verify(documentoPdfRepository).save(captor.capture());
        assertEquals(Sha256.calcular(archivo.getContenido()), captor.getValue().getHashDocumento());
        verify(auditoriaService).registrarEnContrato(eq(usuario), eq("Descarga"), eq(10), eq(7), anyString(),
                eq("10.0.0.1"), isNull(), isNull());
    }

    @Test
    void pdfYaGeneradoSeReutiliza() {
        DocumentoPdf guardado = new DocumentoPdf();
        guardado.setNombreArchivo("guardado.pdf");
        guardado.setContenidoFinal(new byte[] { 1, 2, 3 });
        when(documentoPdfRepository.findFirstByVersionIdVersionOrderByIdPdfDesc(7)).thenReturn(Optional.of(guardado));

        ArchivoPdf archivo = service.descargarFirmado(10, usuario, "10.0.0.1");

        assertEquals("guardado.pdf", archivo.getNombreArchivo());
        verify(documentoPdfRepository, never()).save(any(DocumentoPdf.class));
    }

    @Test
    void contratoNoFirmadoSoloPermiteVersionSinFirmas() throws Exception {
        contrato.getEstado().setNombre(EstadoContrato.LISTO_PARA_FIRMAR);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.descargarFirmado(10, usuario, "10.0.0.1"));
        assertEquals(DescargaPdfService.MENSAJE_NO_FIRMADO, ex.getMessage());

        ArchivoPdf sinFirmas = service.descargarSinFirmas(10, usuario, "10.0.0.1");
        assertEquals("Locación Av. Corrientes_v5.pdf", sinFirmas.getNombreArchivo());
        String texto = textoDelPdf(sinFirmas.getContenido());
        assertTrue(texto.contains("Av. Corrientes 1240"));
        assertFalse(texto.contains("Datos de firma"));
        assertFalse(texto.contains(GeneradorPdf.LEYENDA));
    }

    private String textoDelPdf(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
            texto.append(extractor.getTextFromPage(pagina)).append('\n');
        }
        reader.close();
        return texto.toString();
    }
}
