package com.firmaya.core.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.FirmaExternaResponse;
import com.firmaya.core.dto.FirmaRegistradaResponse;
import com.firmaya.core.dto.MensajeResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.TokenSeguridad;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.FirmaRepository;
import com.firmaya.core.repository.TokenSeguridadRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

/**
 * CU-08 – Firmar contrato vía OTP (acceso con el enlace de firma, sin cuenta).
 */
@Service
public class FirmaOtpService {

    static final String MENSAJE_ENLACE_INVALIDO =
            "El enlace de firma no es válido o ha expirado. Solicite un nuevo enlace al dueño del contrato.";
    static final String MENSAJE_ENLACE_BLOQUEADO =
            "El enlace de firma ha sido bloqueado por seguridad. Contacte al dueño del contrato.";
    static final String MENSAJE_YA_FIRMADO = "Su firma ya fue registrada para este contrato.";
    static final String MENSAJE_NO_LISTO = "El contrato no está disponible para firmar.";
    static final String MENSAJE_CODIGO_EXPIRADO = "El código ha expirado. ¿Desea recibir un nuevo código?";
    static final String MENSAJE_SIN_CODIGO = "Debe solicitar un código de verificación.";
    static final String MENSAJE_FIRMA_REGISTRADA =
            "Su firma ha sido registrada exitosamente. Puede descargar el contrato firmado.";

    private static final int MINUTOS_VALIDEZ_OTP = 10;
    private static final int MAXIMO_INTENTOS_OTP = 3;

    private final FirmaRepository firmaRepository;
    private final TokenSeguridadRepository tokenSeguridadRepository;
    private final UsuarioContratoRepository usuarioContratoRepository;
    private final ContratoRepository contratoRepository;
    private final EstadoContratoRepository estadoContratoRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final GeneradorToken generadorToken;

    public FirmaOtpService(FirmaRepository firmaRepository, TokenSeguridadRepository tokenSeguridadRepository,
            UsuarioContratoRepository usuarioContratoRepository, ContratoRepository contratoRepository,
            EstadoContratoRepository estadoContratoRepository, NotificacionService notificacionService,
            AuditoriaService auditoriaService, GeneradorToken generadorToken) {
        this.firmaRepository = firmaRepository;
        this.tokenSeguridadRepository = tokenSeguridadRepository;
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.contratoRepository = contratoRepository;
        this.estadoContratoRepository = estadoContratoRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.generadorToken = generadorToken;
    }

    /**
     * Pasos 2 a 7: pantalla de firma con los metadatos y el contenido de la versión a firmar.
     */
    @Transactional(readOnly = true)
    public FirmaExternaResponse obtenerContratoAFirmar(String token) {
        Firma firma = buscarFirmaPendiente(token);
        UsuarioContrato parte = firma.getParte();

        FirmaExternaResponse response = new FirmaExternaResponse();
        response.setIdContrato(parte.getContrato().getIdContrato());
        response.setNombreContrato(parte.getContrato().getNombre());
        response.setNumeroVersion(firma.getVersion().getNumeroVersion());
        response.setHash(firma.getVersion().getHashSha256());
        response.setContenido(firma.getVersion().getContenido());
        response.setNombreFirmante(parte.getNombreParte());
        response.setCorreoFirmante(parte.getCorreoInvitado());
        return response;
    }

    /**
     * Pasos 10 y 11, y "Reenviar código": envía un OTP nuevo de 6 dígitos que vence en 10 minutos.
     */
    @Transactional
    public MensajeResponse enviarCodigo(String token) {
        Firma firma = buscarFirmaPendiente(token);
        UsuarioContrato parte = firma.getParte();

        // Solo el último código enviado es válido
        List<TokenSeguridad> anteriores = tokenSeguridadRepository
                .findByIdParteAndTipoTokenAndUsadoFalse(parte.getIdParte(), TokenSeguridad.TIPO_OTP);
        for (TokenSeguridad anterior : anteriores) {
            anterior.setUsado(true);
        }
        tokenSeguridadRepository.saveAll(anteriores);

        LocalDateTime ahora = LocalDateTime.now();
        TokenSeguridad otp = new TokenSeguridad();
        otp.setUsuario(parte.getUsuario());
        otp.setIdParte(parte.getIdParte());
        otp.setTipoToken(TokenSeguridad.TIPO_OTP);
        otp.setValor(generadorToken.generarCodigoOtp());
        otp.setUsado(false);
        otp.setFechaCreacion(ahora);
        otp.setFechaExpiracion(ahora.plusMinutes(MINUTOS_VALIDEZ_OTP));
        tokenSeguridadRepository.save(otp);

        String mensaje = "Hola " + parte.getNombreParte() + ",\n\n"
                + "Tu código para firmar el contrato \"" + parte.getContrato().getNombre() + "\" es: "
                + otp.getValor() + "\n\nEl código expira en " + MINUTOS_VALIDEZ_OTP + " minutos.";
        notificacionService.enviarCorreo(parte.getUsuario(), parte.getContrato().getIdContrato(), "Código OTP",
                parte.getCorreoInvitado(), "FirmaYA - Código de verificación", mensaje);

        return new MensajeResponse("Ingrese el código de 6 dígitos enviado a " + parte.getCorreoInvitado()
                + ". El código expira en " + MINUTOS_VALIDEZ_OTP + " minutos");
    }

    /**
     * Pasos 15 a 20 y caminos alternativos de código incorrecto o expirado.
     * noRollbackFor: los intentos fallidos y el bloqueo deben guardarse aunque se rechace el código.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public FirmaRegistradaResponse confirmarFirma(String token, String codigo, String direccionIp) {
        Firma firma = buscarFirmaPendiente(token);
        UsuarioContrato parte = firma.getParte();

        TokenSeguridad otp = tokenSeguridadRepository
                .findFirstByIdParteAndTipoTokenAndUsadoFalseOrderByIdTokenDesc(parte.getIdParte(),
                        TokenSeguridad.TIPO_OTP)
                .orElseThrow(() -> new ReglaNegocioException(MENSAJE_SIN_CODIGO));

        // Paso 15: código no expirado (menos de 10 minutos desde el envío)
        if (LocalDateTime.now().isAfter(otp.getFechaExpiracion())) {
            throw new ReglaNegocioException(MENSAJE_CODIGO_EXPIRADO);
        }

        // Paso 15: código correcto; tras 3 intentos fallidos consecutivos se bloquea el enlace
        if (!otp.getValor().equals(codigo)) {
            registrarIntentoFallido(firma);
        }

        // Paso 16: firma con fecha, hora, IP, versión y hash
        LocalDateTime ahora = LocalDateTime.now();
        otp.setUsado(true);
        tokenSeguridadRepository.save(otp);

        firma.setEstadoFirma(Firma.ESTADO_FIRMADO);
        firma.setFechaFirma(ahora);
        firma.setFechaUltimoEvento(ahora);
        firma.setDireccionIp(direccionIp);
        firma.setHashFirma(firma.getVersion().getHashSha256());
        firma.setIntentosOtpFallidos(0);
        firmaRepository.save(firma);

        Contrato contrato = parte.getContrato();
        auditoriaService.registrarEnContrato(parte.getUsuario(), "Firma", contrato.getIdContrato(),
                firma.getVersion().getIdVersion(), "Firma de " + parte.getNombreParte() + " ("
                        + parte.getCorreoInvitado() + ") sobre la versión " + firma.getVersion().getNumeroVersion(),
                direccionIp, null, null);

        // Pasos 18 y 19: si todos firmaron, el contrato pasa a "Firmado" y se avisa al responsable
        boolean todosFirmaron = todosLosFirmantesFirmaron(contrato.getIdContrato());
        if (todosFirmaron) {
            contrato.setEstado(estadoContratoRepository.findByNombre(EstadoContrato.FIRMADO)
                    .orElseThrow(() -> new IllegalStateException("No existe el estado de contrato Firmado")));
            contratoRepository.save(contrato);
            auditoriaService.registrarEnContrato(null, "Cambio de estado", contrato.getIdContrato(), null,
                    "Cambio de estado de Listo para firmar a Firmado: firmaron todos los firmantes", direccionIp,
                    EstadoContrato.LISTO_PARA_FIRMAR, EstadoContrato.FIRMADO);

            Usuario responsable = contrato.getUsuarioCreador();
            if (responsable != null) {
                notificacionService.notificarEvento(responsable, responsable.getEmail(), contrato.getIdContrato(),
                        PreferenciaNotificacion.EVENTO_FIRMA_RECIBIDA, "FirmaYA - Contrato firmado",
                        "El contrato \"" + contrato.getNombre() + "\" fue firmado por todos los firmantes.");
            }
        }

        // Paso 20
        FirmaRegistradaResponse response = new FirmaRegistradaResponse();
        response.setIdContrato(contrato.getIdContrato());
        response.setContratoFirmado(todosFirmaron);
        response.setMensaje(MENSAJE_FIRMA_REGISTRADA);
        return response;
    }

    private void registrarIntentoFallido(Firma firma) {
        int intentos = firma.getIntentosOtpFallidos() == null ? 0 : firma.getIntentosOtpFallidos();
        intentos++;
        firma.setIntentosOtpFallidos(intentos);
        if (intentos >= MAXIMO_INTENTOS_OTP) {
            firma.setEnlaceBloqueado(true);
            firmaRepository.save(firma);
            throw new ReglaNegocioException(MENSAJE_ENLACE_BLOQUEADO);
        }
        firmaRepository.save(firma);
        throw new ReglaNegocioException("El código ingresado es incorrecto. Verifique el código e intente "
                + "nuevamente. Intentos restantes: " + (MAXIMO_INTENTOS_OTP - intentos) + ".");
    }

    private boolean todosLosFirmantesFirmaron(Integer idContrato) {
        List<UsuarioContrato> firmantes = usuarioContratoRepository
                .findByContratoIdContratoAndRolParteOrderByIdParte(idContrato, UsuarioContrato.ROL_FIRMANTE);
        for (UsuarioContrato firmante : firmantes) {
            Firma firma = firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(firmante.getIdParte())
                    .orElse(null);
            if (firma == null || !Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
                return false;
            }
        }
        return !firmantes.isEmpty();
    }

    /**
     * Paso 2 y precondiciones: enlace válido, vigente y no bloqueado; contrato "Listo para firmar";
     * el firmante todavía no firmó.
     */
    private Firma buscarFirmaPendiente(String token) {
        Firma firma = firmaRepository.findByTokenFirma(token).orElse(null);
        if (firma == null || firma.getFechaExpiracionToken() == null
                || LocalDateTime.now().isAfter(firma.getFechaExpiracionToken())) {
            throw new NoAutenticadoException(MENSAJE_ENLACE_INVALIDO);
        }
        if (Boolean.TRUE.equals(firma.getEnlaceBloqueado())) {
            throw new ReglaNegocioException(MENSAJE_ENLACE_BLOQUEADO);
        }
        if (Firma.ESTADO_FIRMADO.equals(firma.getEstadoFirma())) {
            throw new ReglaNegocioException(MENSAJE_YA_FIRMADO);
        }
        if (!EstadoContrato.LISTO_PARA_FIRMAR.equals(firma.getParte().getContrato().getEstado().getNombre())) {
            throw new ReglaNegocioException(MENSAJE_NO_LISTO);
        }
        return firma;
    }
}
