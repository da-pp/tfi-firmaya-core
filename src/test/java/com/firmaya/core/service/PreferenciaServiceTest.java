package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.firmaya.core.dto.OpcionPreferencia;
import com.firmaya.core.dto.PreferenciasRequest;
import com.firmaya.core.dto.PreferenciasResponse;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.PreferenciaNotificacionRepository;

class PreferenciaServiceTest {

    private PreferenciaNotificacionRepository repository;
    private PreferenciaService service;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        repository = mock(PreferenciaNotificacionRepository.class);
        service = new PreferenciaService(repository);
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        when(repository.findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(any(), any()))
                .thenReturn(Optional.empty());
        when(repository.findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(any(), any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void sinPreferenciasGuardadasTodoActivo() {
        PreferenciasResponse response = service.obtenerPreferencias(usuario);

        assertEquals(5, response.getEventos().size());
        assertEquals(2, response.getCanales().size());
        for (OpcionPreferencia opcion : response.getEventos()) {
            assertTrue(opcion.isActivo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void guardarReemplazaLasFilasDelUsuario() {
        PreferenciasRequest request = new PreferenciasRequest();
        request.setEventos(Arrays.asList(new OpcionPreferencia(PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO, false)));
        request.setCanales(Arrays.asList(new OpcionPreferencia(PreferenciaNotificacion.CANAL_PLATAFORMA, false)));

        PreferenciasResponse response = service.guardarPreferencias(usuario, request);

        verify(repository).deleteByUsuarioIdUsuario(1);
        ArgumentCaptor<List<PreferenciaNotificacion>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(7, captor.getValue().size());
        assertEquals(PreferenciaService.MENSAJE_GUARDADAS, response.getMensaje());
        assertFalse(buscar(response.getEventos(), PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO).isActivo());
        assertTrue(buscar(response.getCanales(), PreferenciaNotificacion.CANAL_CORREO).isActivo());
    }

    @Test
    void sinCanalSeleccionadoSeBloqueaElGuardado() {
        PreferenciasRequest request = new PreferenciasRequest();
        request.setCanales(Arrays.asList(new OpcionPreferencia(PreferenciaNotificacion.CANAL_CORREO, false),
                new OpcionPreferencia(PreferenciaNotificacion.CANAL_PLATAFORMA, false)));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.guardarPreferencias(usuario, request));

        assertEquals(PreferenciaService.MENSAJE_SIN_CANAL, ex.getMessage());
        verify(repository, never()).deleteByUsuarioIdUsuario(any());
    }

    @Test
    void todosLosEventosDesactivadosPideConfirmacion() {
        PreferenciasRequest request = new PreferenciasRequest();
        List<OpcionPreferencia> eventos = new ArrayList<>();
        for (String evento : PreferenciaNotificacion.EVENTOS) {
            eventos.add(new OpcionPreferencia(evento, false));
        }
        request.setEventos(eventos);

        ConfirmacionRequeridaException ex = assertThrows(ConfirmacionRequeridaException.class,
                () -> service.guardarPreferencias(usuario, request));
        assertEquals(PreferenciaService.MENSAJE_TODO_DESACTIVADO, ex.getMessage());

        // Con "Confirmar" se guarda
        request.setConfirmarTodoDesactivado(true);
        assertEquals(PreferenciaService.MENSAJE_GUARDADAS, service.guardarPreferencias(usuario, request).getMensaje());
    }

    @Test
    void eventoDesconocidoEsRechazado() {
        PreferenciasRequest request = new PreferenciasRequest();
        request.setEventos(Arrays.asList(new OpcionPreferencia("Contrato próximo a vencer", true)));

        assertThrows(ReglaNegocioException.class, () -> service.guardarPreferencias(usuario, request));
    }

    private OpcionPreferencia buscar(List<OpcionPreferencia> opciones, String nombre) {
        for (OpcionPreferencia opcion : opciones) {
            if (opcion.getNombre().equals(nombre)) {
                return opcion;
            }
        }
        return null;
    }
}
