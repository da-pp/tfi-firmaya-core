package com.firmaya.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.EstadoFirmasResponse;
import com.firmaya.core.dto.FirmanteResponse;
import com.firmaya.core.dto.SolicitarFirmasRequest;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.FirmaRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

/**
 * CU-07 – Solicitar firma de las partes y CU-09 – Consultar estado de firmas pendientes.
 */
@Service
public class SolicitudFirmaService {

    static final String MENSAJE_SIN_FIRMANTES = "No hay firmantes asignados. Debe invitar al menos a una parte "
            + "con el rol de Firmante antes de solicitar firmas.";
    static final String MENSAJE_NO_LISTO = "El contrato debe estar en estado Listo para firmar para solicitar firmas.";
    static final String MENSAJE_SOLICITUDES_ENVIADAS = "Solicitudes de firma enviadas exitosamente";
    static final String MENSAJE_SIN_SOLICITUDES = "Aún no se han enviado solicitudes de firma para este contrato.";
    static final String MENSAJE_TODAS_COMPLETADAS = "Todas las firmas han sido completadas. El contrato está firmado.";
    static final String MENSAJE_REENVIADA = "Solicitud reenviada exitosamente";
    static final String MENSAJE_ERROR_REENVIO =
            "No se pudo reenviar la solicitud. ¿Desea reintentar o copiar el enlace manualmente?";
    static final String TIPO_NOTIFICACION = "Solicitud de firma";
    static final String ASUNTO_CORREO = "FirmaYA - Solicitud de firma";

    // Duración de los tokens sin plazo definido en el documento
    static final int MINUTOS_TOKEN_FIRMA = 15;

    private final FirmaRepository firmaRepository;
    private final UsuarioContratoRepository usuarioContratoRepository;
    private final ContratoService contratoService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final GeneradorToken generadorToken;
    private final String frontendUrl;

    public SolicitudFirmaService(FirmaRepository firmaRepository,
            UsuarioContratoRepository usuarioContratoRepository, ContratoService contratoService,
            NotificacionService notificacionService, AuditoriaService auditoriaService,
            GeneradorToken generadorToken, @Value("${firmaya.frontend-url}") String frontendUrl) {
        this.firmaRepository = firmaRepository;
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.contratoService = contratoService;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.generadorToken = generadorToken;
        this.frontendUrl = frontendUrl;
    }

    /**
     * CU-07 pasos 2 a 6: firmantes del contrato con el estado de su firma.
     */
    @Transactional(readOnly = true)
    public EstadoFirmasResponse listarFirmantes(Integer idContrato) {
        contratoService.buscarContrato(idContrato);
        EstadoFirmasResponse response = armarEstado(idContrato, new HashMap<Integer, Boolean>());
        if (response.getTotalFirmantes() == 0) {
            response.setMensaje(MENSAJE_SIN_FIRMANTES);
        }
        return response;
    }

    /**
     * CU-07 pasos 12 a 19.
     */
    @Transactional
    public EstadoFirmasResponse solicitarFirmas(Integer idContrato, SolicitarFirmasRequest request, Usuario usuario,
            String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        if (!EstadoContrato.LISTO_PARA_FIRMAR.equals(contrato.getEstado().getNombre())) {
            throw new ReglaNegocioException(MENSAJE_NO_LISTO);
        }
        List<UsuarioContrato> firmantes = firmantes(idContrato);
        if (firmantes.isEmpty()) {
            throw new ReglaNegocioException(MENSAJE_SIN_FIRMANTES);
        }

        // Paso 13: la fecha límite, si se ingresó, debe ser posterior a hoy
        LocalDate fechaLimite = null;
        if (request.getFechaLimite() != null && !request.getFechaLimite().trim().isEmpty()) {
            fechaLimite = Fechas.leer(request.getFechaLimite(), "fechaLimite", "La fecha debe tener el formato DD/MM/AAAA");
            if (!fechaLimite.isAfter(LocalDate.now())) {
                throw new ReglaNegocioException("fechaLimite", SolicitarFirmasRequest.MENSAJE_FECHA_LIMITE);
            }
        }

        // Pasos 14 a 17: enlace único por firmante, registro de la solicitud y envío
        VersionContrato version = contratoService.versionActual(idContrato);
        Map<Integer, Boolean> envios = new HashMap<>();
        for (UsuarioContrato firmante : firmantes) {
            Firma firma = firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(firmante.getIdParte())
                    .orElse(null);
            if (firma != null && Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
                continue;
            }
            if (firma == null) {
                firma = new Firma();
                firma.setParte(firmante);
            }
            firma.setVersion(version);
            firma.setEstadoFirma(Firma.ESTADO_PENDIENTE);
            firma.setFechaLimite(fechaLimite);
            firma.setIntentosOtpFallidos(0);
            firma.setEnlaceBloqueado(false);
            boolean enviado = enviarSolicitud(firma, request.getMensaje(), Firma.ESTADO_NOTIFICADO);
            envios.put(firma.getIdFirma(), enviado);
        }

        auditoriaService.registrarEnContrato(usuario, "Solicitud de firma", idContrato, version.getIdVersion(),
                "Solicitud de firma por " + request.getCanal()
                        + (fechaLimite == null ? "" : " con fecha límite " + Fechas.formatear(fechaLimite)),
                direccionIp, null, null);

        EstadoFirmasResponse response = armarEstado(idContrato, envios);
        if (!envios.containsValue(false)) {
            response.setMensaje(MENSAJE_SOLICITUDES_ENVIADAS);
        }
        return response;
    }

    /**
     * CU-07 camino alternativo "Fallo en el envío de notificaciones", botón "Reintentar fallidos".
     */
    @Transactional
    public EstadoFirmasResponse reintentarFallidos(Integer idContrato, List<Integer> idsFirma) {
        Map<Integer, Boolean> envios = new HashMap<>();
        for (Integer idFirma : idsFirma) {
            Firma firma = buscarFirma(idContrato, idFirma);
            if (Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
                continue;
            }
            UsuarioContrato parte = firma.getParte();
            boolean enviado = notificacionService.reenviarUltimoCorreo(parte.getUsuario(), idContrato,
                    TIPO_NOTIFICACION, parte.getCorreoInvitado(), ASUNTO_CORREO);
            if (enviado) {
                firma.setEstadoFirma(Firma.ESTADO_NOTIFICADO);
                firma.setFechaUltimoEvento(LocalDateTime.now());
                firmaRepository.save(firma);
            }
            envios.put(idFirma, enviado);
        }
        EstadoFirmasResponse response = armarEstado(idContrato, envios);
        if (!envios.containsValue(false)) {
            response.setMensaje(MENSAJE_SOLICITUDES_ENVIADAS);
        }
        return response;
    }

    /**
     * CU-09 pasos 2 a 10 y 19.
     */
    @Transactional(readOnly = true)
    public EstadoFirmasResponse consultarEstado(Integer idContrato) {
        contratoService.buscarContrato(idContrato);
        // Camino alternativo: contrato sin solicitudes de firma enviadas
        if (firmaRepository.findByParteContratoIdContrato(idContrato).isEmpty()) {
            EstadoFirmasResponse response = new EstadoFirmasResponse();
            response.setFirmantes(new ArrayList<FirmanteResponse>());
            response.setMensaje(MENSAJE_SIN_SOLICITUDES);
            return response;
        }
        EstadoFirmasResponse response = armarEstado(idContrato, new HashMap<Integer, Boolean>());
        if (response.isTodasCompletadas()) {
            response.setMensaje(MENSAJE_TODAS_COMPLETADAS);
        }
        return response;
    }

    /**
     * CU-09 pasos 15 a 17: reenvía la solicitud con un enlace nuevo y marca "Re-notificado".
     */
    @Transactional
    public EstadoFirmasResponse reenviarSolicitud(Integer idContrato, Integer idFirma, Usuario usuario,
            String direccionIp) {
        Firma firma = buscarFirma(idContrato, idFirma);
        if (Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
            throw new ReglaNegocioException("La firma ya fue completada.");
        }
        boolean enviado = enviarSolicitud(firma, null, Firma.ESTADO_RENOTIFICADO);

        auditoriaService.registrarEnContrato(usuario, "Reenvío de solicitud de firma", idContrato, null,
                "Reenvío de la solicitud de firma a " + firma.getParte().getNombreParte(), direccionIp, null, null);

        Map<Integer, Boolean> envios = new HashMap<>();
        envios.put(idFirma, enviado);
        EstadoFirmasResponse response = armarEstado(idContrato, envios);
        response.setMensaje(enviado ? MENSAJE_REENVIADA : MENSAJE_ERROR_REENVIO);
        return response;
    }

    String enlace(Firma firma) {
        return frontendUrl + "/firmar/" + firma.getTokenFirma();
    }

    /**
     * Genera un enlace de firma nuevo (15 minutos) y lo envía por correo.
     * Si el envío funciona, la firma pasa al estado indicado.
     */
    private boolean enviarSolicitud(Firma firma, String mensajePersonalizado, String estadoSiSeEnvia) {
        LocalDateTime ahora = LocalDateTime.now();
        firma.setTokenFirma(generadorToken.generar());
        firma.setFechaExpiracionToken(ahora.plusMinutes(MINUTOS_TOKEN_FIRMA));
        firmaRepository.save(firma);

        UsuarioContrato parte = firma.getParte();
        Contrato contrato = parte.getContrato();
        StringBuilder correo = new StringBuilder();
        correo.append("Hola ").append(parte.getNombreParte()).append(",\n\n");
        correo.append("Se solicita tu firma en el contrato \"").append(contrato.getNombre()).append("\".\n\n");
        if (mensajePersonalizado != null && !mensajePersonalizado.trim().isEmpty()) {
            correo.append(mensajePersonalizado.trim()).append("\n\n");
        }
        if (firma.getFechaLimite() != null) {
            correo.append("Fecha límite para firmar: ").append(Fechas.formatear(firma.getFechaLimite())).append("\n\n");
        }
        correo.append("Para leer y firmar el contrato ingresá al siguiente enlace:\n").append(enlace(firma))
                .append("\n\nEl enlace expira en ").append(MINUTOS_TOKEN_FIRMA).append(" minutos.");

        boolean enviado = notificacionService.enviarCorreo(parte.getUsuario(), contrato.getIdContrato(),
                TIPO_NOTIFICACION, parte.getCorreoInvitado(), ASUNTO_CORREO, correo.toString());
        if (enviado) {
            firma.setEstadoFirma(estadoSiSeEnvia);
            firma.setFechaUltimoEvento(ahora);
            firmaRepository.save(firma);
        }
        return enviado;
    }

    private EstadoFirmasResponse armarEstado(Integer idContrato, Map<Integer, Boolean> envios) {
        List<FirmanteResponse> lista = new ArrayList<>();
        int completadas = 0;
        for (UsuarioContrato firmante : firmantes(idContrato)) {
            Firma firma = firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(firmante.getIdParte())
                    .orElse(null);
            FirmanteResponse fila = new FirmanteResponse();
            fila.setIdParte(firmante.getIdParte());
            fila.setNombre(firmante.getNombreParte());
            fila.setEmail(firmante.getCorreoInvitado());
            fila.setEstadoFirma(Firma.ESTADO_PENDIENTE);
            if (firma != null) {
                fila.setIdFirma(firma.getIdFirma());
                fila.setEstadoFirma(firma.getEstadoFirma());
                fila.setEnvioExitoso(envios.get(firma.getIdFirma()));
                if (Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
                    completadas++;
                    fila.setFechaEvento(firma.getFechaFirma());
                    fila.setHashFirma(firma.getHashFirma());
                    fila.setDireccionIp(firma.getDireccionIp());
                } else {
                    fila.setFechaEvento(firma.getFechaUltimoEvento());
                    fila.setEnlace(firma.getTokenFirma() == null ? null : enlace(firma));
                }
            }
            lista.add(fila);
        }

        EstadoFirmasResponse response = new EstadoFirmasResponse();
        response.setFirmantes(lista);
        response.setFirmasCompletadas(completadas);
        response.setTotalFirmantes(lista.size());
        response.setProgreso(completadas + " de " + lista.size() + " firmas completadas");
        response.setTodasCompletadas(!lista.isEmpty() && completadas == lista.size());
        return response;
    }

    private List<UsuarioContrato> firmantes(Integer idContrato) {
        return usuarioContratoRepository.findByContratoIdContratoAndRolParteOrderByIdParte(idContrato,
                UsuarioContrato.ROL_FIRMANTE);
    }

    private Firma buscarFirma(Integer idContrato, Integer idFirma) {
        return firmaRepository.findByIdFirmaAndParteContratoIdContrato(idFirma, idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException("Firma no encontrada"));
    }
}
