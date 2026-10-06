package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
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
        assertTrue(service.eventoActivo(usuario, PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO));
        assertTrue(service.canalActivo(usuario, PreferenciaNotificacion.CANAL_CORREO));
    }

    @Test
    void preferenciaGuardadaInactivaDesactivaEventoYCanal() {
        PreferenciaNotificacion inactiva = new PreferenciaNotificacion();
        inactiva.setActiva(false);
        when(repository.findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(1,
                PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO)).thenReturn(Optional.of(inactiva));
        when(repository.findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(1,
                PreferenciaNotificacion.CANAL_PLATAFORMA)).thenReturn(Optional.of(inactiva));

        assertFalse(service.eventoActivo(usuario, PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO));
        assertFalse(service.canalActivo(usuario, PreferenciaNotificacion.CANAL_PLATAFORMA));
    }
}
