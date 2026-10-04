package com.firmaya.core.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.ArchivoPdf;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.DocumentoPdf;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.DocumentoPdfRepository;
import com.firmaya.core.repository.FirmaRepository;

/**
 * CU-10 – Descargar contrato firmado en PDF (usuarios internos y partes externas).
 */
@Service
public class DescargaPdfService {

    static final String MENSAJE_NO_FIRMADO = "El PDF completo solo está disponible una vez que todos los "
            + "firmantes hayan completado la firma";

    private final ContratoService contratoService;
    private final AccesoExternoService accesoExternoService;
    private final FirmaRepository firmaRepository;
    private final DocumentoPdfRepository documentoPdfRepository;
    private final GeneradorPdf generadorPdf;
    private final AuditoriaService auditoriaService;

    public DescargaPdfService(ContratoService contratoService, AccesoExternoService accesoExternoService,
            FirmaRepository firmaRepository, DocumentoPdfRepository documentoPdfRepository, GeneradorPdf generadorPdf,
            AuditoriaService auditoriaService) {
        this.contratoService = contratoService;
        this.accesoExternoService = accesoExternoService;
        this.firmaRepository = firmaRepository;
        this.documentoPdfRepository = documentoPdfRepository;
        this.generadorPdf = generadorPdf;
        this.auditoriaService = auditoriaService;
    }

    // Usuario interno
    @Transactional
    public ArchivoPdf descargarFirmado(Integer idContrato, Usuario usuario, String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        return pdfFirmado(contrato, usuario, usuario.getNombre() + " " + usuario.getApellido(), direccionIp);
    }

    @Transactional
    public ArchivoPdf descargarSinFirmas(Integer idContrato, Usuario usuario, String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        return pdfSinFirmas(contrato, usuario, usuario.getNombre() + " " + usuario.getApellido(), direccionIp);
    }

    // Parte invitada, con el token de invitación (CU-04 paso 16)
    @Transactional
    public ArchivoPdf descargarFirmadoExterno(String token, String direccionIp) {
        UsuarioContrato parte = accesoExternoService.buscarParteConAcceso(token);
        return pdfFirmado(parte.getContrato(), parte.getUsuario(), parte.getNombreParte(), direccionIp);
    }

    @Transactional
    public ArchivoPdf descargarSinFirmasExterno(String token, String direccionIp) {
        UsuarioContrato parte = accesoExternoService.buscarParteConAcceso(token);
        return pdfSinFirmas(parte.getContrato(), parte.getUsuario(), parte.getNombreParte(), direccionIp);
    }

    // Firmante, con el enlace de firma (CU-08 paso 21)
    @Transactional
    public ArchivoPdf descargarFirmadoConEnlaceDeFirma(String token, String direccionIp) {
        Firma firma = firmaRepository.findByTokenFirma(token).orElse(null);
        if (firma == null || firma.getFechaExpiracionToken() == null
                || LocalDateTime.now().isAfter(firma.getFechaExpiracionToken())) {
            throw new NoAutenticadoException(FirmaOtpService.MENSAJE_ENLACE_INVALIDO);
        }
        UsuarioContrato parte = firma.getParte();
        return pdfFirmado(parte.getContrato(), parte.getUsuario(), parte.getNombreParte(), direccionIp);
    }

    /**
     * Pasos 3 a 17. El PDF se guarda en DOCUMENTO_PDF y se reutiliza en las descargas siguientes.
     */
    private ArchivoPdf pdfFirmado(Contrato contrato, Usuario usuario, String quienDescarga, String direccionIp) {
        // Paso 3 / camino alternativo: el contrato debe estar Firmado
        if (!EstadoContrato.FIRMADO.equals(contrato.getEstado().getNombre())) {
            throw new ReglaNegocioException(MENSAJE_NO_FIRMADO);
        }
        VersionContrato version = contratoService.versionActual(contrato.getIdContrato());

        DocumentoPdf documento = documentoPdfRepository
                .findFirstByVersionIdVersionOrderByIdPdfDesc(version.getIdVersion()).orElse(null);
        if (documento == null) {
            byte[] pdf = generadorPdf.generar(contrato, version, firmasCompletadas(contrato.getIdContrato()));
            documento = new DocumentoPdf();
            documento.setVersion(version);
            // Paso 12: [NombreDelContrato]_v[versión]_firmado.pdf
            documento.setNombreArchivo(nombreArchivo(contrato) + "_v" + version.getNumeroVersion() + "_firmado.pdf");
            documento.setContenidoFinal(pdf);
            documento.setFechaGeneracion(LocalDateTime.now());
            documento.setHashDocumento(Sha256.calcular(pdf));
            documentoPdfRepository.save(documento);
        }

        // Paso 17: usuario que descargó, fecha, hora y versión
        auditoriaService.registrarEnContrato(usuario, "Descarga", contrato.getIdContrato(), version.getIdVersion(),
                "Descarga del PDF firmado (versión " + version.getNumeroVersion() + ") por " + quienDescarga,
                direccionIp, null, null);
        return new ArchivoPdf(documento.getNombreArchivo(), documento.getContenidoFinal());
    }

    // Camino alternativo "Descargar versión actual sin firmas"
    private ArchivoPdf pdfSinFirmas(Contrato contrato, Usuario usuario, String quienDescarga, String direccionIp) {
        VersionContrato version = contratoService.versionActual(contrato.getIdContrato());
        byte[] pdf = generadorPdf.generar(contrato, version, null);
        auditoriaService.registrarEnContrato(usuario, "Descarga", contrato.getIdContrato(), version.getIdVersion(),
                "Descarga del PDF sin firmas (versión " + version.getNumeroVersion() + ") por " + quienDescarga,
                direccionIp, null, null);
        return new ArchivoPdf(nombreArchivo(contrato) + "_v" + version.getNumeroVersion() + ".pdf", pdf);
    }

    private List<Firma> firmasCompletadas(Integer idContrato) {
        List<Firma> completadas = new ArrayList<>();
        for (Firma firma : firmaRepository.findByParteContratoIdContrato(idContrato)) {
            if (Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
                completadas.add(firma);
            }
        }
        return completadas;
    }

    // Quita los caracteres no permitidos en nombres de archivo
    private String nombreArchivo(Contrato contrato) {
        return contrato.getNombre().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
