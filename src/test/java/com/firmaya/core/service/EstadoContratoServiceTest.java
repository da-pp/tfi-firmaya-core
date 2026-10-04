package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.CambiarEstadoRequest;
import com.firmaya.core.dto.CambioEstadoResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.TransicionEstadoRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

class EstadoContratoServiceTest {

    private ContratoService contratoService;
    private ContratoRepository contratoRepository;
    private EstadoContratoRepository estadoContratoRepository;
    private TransicionEstadoRepository transicionEstadoRepository;
    private UsuarioContratoRepository usuarioContratoRepository;
    private NotificacionService notificacionService;
    private AuditoriaService auditoriaService;
    private EstadoContratoService service;
    private Usuario usuario = new Usuario();
    private Contrato contrato;

    @BeforeEach
    void preparar() {
        contratoService = mock(ContratoService.class);
        contratoRepository = mock(ContratoRepository.class);
        estadoContratoRepository = mock(EstadoContratoRepository.class);
        transicionEstadoRepository = mock(TransicionEstadoRepository.class);
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        notificacionService = mock(NotificacionService.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new EstadoContratoService(contratoService, contratoRepository, estadoContratoRepository,
                transicionEstadoRepository, usuarioContratoRepository, notificacionService, auditoriaService);

        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación");
        contrato.setEstado(estado(2, EstadoContrato.EN_REVISION));
        when(contratoService.buscarContrato(10)).thenReturn(contrato);
        when(estadoContratoRepository.findByNombre(EstadoContrato.LISTO_PARA_FIRMAR))
                .thenReturn(Optional.of(estado(3, EstadoContrato.LISTO_PARA_FIRMAR)));
        when(estadoContratoRepository.findByNombre(EstadoContrato.BORRADOR))
                .thenReturn(Optional.of(estado(1, EstadoContrato.BORRADOR)));
        when(transicionEstadoRepository.existsByEstadoOrigenIdEstadoAndEstadoDestinoIdEstado(2, 3)).thenReturn(true);
    }

    @Test
    void transicionPermitidaActualizaAuditaYNotifica() {
        UsuarioContrato firmante = parte("ana@mail.com");
        when(usuarioContratoRepository.existsByContratoIdContratoAndRolParte(10, UsuarioContrato.ROL_FIRMANTE))
                .thenReturn(true);
        when(usuarioContratoRepository.findByContratoIdContratoOrderByIdParte(10))
                .thenReturn(Arrays.asList(firmante));

        CambioEstadoResponse response = service.cambiarEstado(10, request(EstadoContrato.LISTO_PARA_FIRMAR, false),
                usuario, "10.0.0.1");

        assertEquals(EstadoContrato.EN_REVISION, response.getEstadoAnterior());
        assertEquals(EstadoContrato.LISTO_PARA_FIRMAR, contrato.getEstado().getNombre());
        assertEquals(EstadoContratoService.MENSAJE_ACTUALIZADO, response.getMensaje());
        verify(auditoriaService).registrarEnContrato(eq(usuario), eq("Cambio de estado"), eq(10), isNull(),
                anyString(), eq("10.0.0.1"), eq(EstadoContrato.EN_REVISION), eq(EstadoContrato.LISTO_PARA_FIRMAR));
        verify(notificacionService).notificarEvento(isNull(), eq("ana@mail.com"), eq(10),
                eq(PreferenciaNotificacion.EVENTO_LISTO_PARA_FIRMAR), anyString(), anyString());
    }

    @Test
    void transicionNoPermitidaEsRechazada() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.cambiarEstado(10, request(EstadoContrato.BORRADOR, false), usuario, "10.0.0.1"));

        assertEquals(EstadoContratoService.MENSAJE_TRANSICION_INVALIDA, ex.getMessage());
        verify(contratoRepository, never()).save(any(Contrato.class));
    }

    @Test
    void listoParaFirmarSinFirmantesPideConfirmacion() {
        ConfirmacionRequeridaException ex = assertThrows(ConfirmacionRequeridaException.class,
                () -> service.cambiarEstado(10, request(EstadoContrato.LISTO_PARA_FIRMAR, false), usuario,
                        "10.0.0.1"));
        assertEquals(EstadoContratoService.MENSAJE_SIN_FIRMANTES, ex.getMessage());

        // Con "Continuar" el cambio se realiza
        service.cambiarEstado(10, request(EstadoContrato.LISTO_PARA_FIRMAR, true), usuario, "10.0.0.1");
        assertEquals(EstadoContrato.LISTO_PARA_FIRMAR, contrato.getEstado().getNombre());
    }

    private UsuarioContrato parte(String correo) {
        UsuarioContrato parte = new UsuarioContrato();
        parte.setCorreoInvitado(correo);
        return parte;
    }

    private CambiarEstadoRequest request(String nuevoEstado, boolean continuar) {
        CambiarEstadoRequest request = new CambiarEstadoRequest();
        request.setNuevoEstado(nuevoEstado);
        request.setContinuarSinFirmantes(continuar);
        return request;
    }

    private EstadoContrato estado(int id, String nombre) {
        EstadoContrato estado = new EstadoContrato();
        estado.setIdEstado(id);
        estado.setNombre(nombre);
        return estado;
    }
}
