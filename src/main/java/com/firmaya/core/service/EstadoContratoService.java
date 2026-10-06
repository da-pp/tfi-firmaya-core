package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.CambioEstadoResponse;
import com.firmaya.core.dto.CambiarEstadoRequest;
import com.firmaya.core.dto.TransicionesResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.TransicionEstado;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.TransicionEstadoRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

@Service
public class EstadoContratoService {

    static final String MENSAJE_TRANSICION_INVALIDA =
            "Esta transición de estado no es posible. Verifique el flujo permitido.";
    static final String MENSAJE_SIN_FIRMANTES =
            "El contrato no tiene firmantes asignados. ¿Desea continuar o ir a invitar partes?";
    static final String MENSAJE_ACTUALIZADO = "Estado actualizado exitosamente";

    private final ContratoService contratoService;
    private final ContratoRepository contratoRepository;
    private final EstadoContratoRepository estadoContratoRepository;
    private final TransicionEstadoRepository transicionEstadoRepository;
    private final UsuarioContratoRepository usuarioContratoRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    public EstadoContratoService(ContratoService contratoService, ContratoRepository contratoRepository,
            EstadoContratoRepository estadoContratoRepository, TransicionEstadoRepository transicionEstadoRepository,
            UsuarioContratoRepository usuarioContratoRepository, NotificacionService notificacionService,
            AuditoriaService auditoriaService) {
        this.contratoService = contratoService;
        this.contratoRepository = contratoRepository;
        this.estadoContratoRepository = estadoContratoRepository;
        this.transicionEstadoRepository = transicionEstadoRepository;
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public TransicionesResponse obtenerTransiciones(Integer idContrato) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        List<String> disponibles = new ArrayList<>();
        for (TransicionEstado transicion : transicionEstadoRepository
                .findByEstadoOrigenIdEstado(contrato.getEstado().getIdEstado())) {
            disponibles.add(transicion.getEstadoDestino().getNombre());
        }
        TransicionesResponse response = new TransicionesResponse();
        response.setEstadoActual(contrato.getEstado().getNombre());
        response.setEstadosDisponibles(disponibles);
        return response;
    }

    @Transactional
    public CambioEstadoResponse cambiarEstado(Integer idContrato, CambiarEstadoRequest request, Usuario usuario,
            String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        EstadoContrato anterior = contrato.getEstado();

        EstadoContrato nuevo = estadoContratoRepository.findByNombre(request.getNuevoEstado().trim()).orElse(null);
        if (nuevo == null || !transicionEstadoRepository
                .existsByEstadoOrigenIdEstadoAndEstadoDestinoIdEstado(anterior.getIdEstado(), nuevo.getIdEstado())) {
            throw new ReglaNegocioException(MENSAJE_TRANSICION_INVALIDA);
        }

        boolean pasaAListoParaFirmar = EstadoContrato.LISTO_PARA_FIRMAR.equals(nuevo.getNombre());
        if (pasaAListoParaFirmar && !request.isContinuarSinFirmantes() && !usuarioContratoRepository
                .existsByContratoIdContratoAndRolParte(idContrato, UsuarioContrato.ROL_FIRMANTE)) {
            throw new ConfirmacionRequeridaException(MENSAJE_SIN_FIRMANTES);
        }

        contrato.setEstado(nuevo);
        contratoRepository.save(contrato);

        String razon = request.getRazon() == null || request.getRazon().trim().isEmpty() ? null
                : request.getRazon().trim();
        auditoriaService.registrarEnContrato(usuario, "Cambio de estado", idContrato, null,
                "Cambio de estado de " + anterior.getNombre() + " a " + nuevo.getNombre()
                        + (razon == null ? "" : ". Razón: " + razon),
                direccionIp, anterior.getNombre(), nuevo.getNombre());

        String evento = pasaAListoParaFirmar ? PreferenciaNotificacion.EVENTO_LISTO_PARA_FIRMAR
                : PreferenciaNotificacion.EVENTO_CAMBIO_ESTADO;
        String mensaje = "El contrato \"" + contrato.getNombre() + "\" cambió de estado de " + anterior.getNombre()
                + " a " + nuevo.getNombre() + "." + (razon == null ? "" : "\n\nRazón del cambio: " + razon);
        for (UsuarioContrato parte : usuarioContratoRepository.findByContratoIdContratoOrderByIdParte(idContrato)) {
            notificacionService.notificarEvento(parte.getUsuario(), parte.getCorreoInvitado(), idContrato, evento,
                    "FirmaYA - Cambio de estado del contrato", mensaje);
        }

        CambioEstadoResponse response = new CambioEstadoResponse();
        response.setEstadoAnterior(anterior.getNombre());
        response.setEstadoNuevo(nuevo.getNombre());
        response.setMensaje(MENSAJE_ACTUALIZADO);
        return response;
    }
}
