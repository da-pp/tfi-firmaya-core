package com.firmaya.core.service;

import org.springframework.stereotype.Service;

import com.firmaya.core.entity.Usuario;
import com.firmaya.core.repository.PreferenciaNotificacionRepository;

/**
 * Consulta de las preferencias de notificación de un usuario.
 * Un evento o canal sin preferencia guardada se considera activo.
 */
@Service
public class PreferenciaService {

    private final PreferenciaNotificacionRepository preferenciaRepository;

    public PreferenciaService(PreferenciaNotificacionRepository preferenciaRepository) {
        this.preferenciaRepository = preferenciaRepository;
    }

    public boolean eventoActivo(Usuario usuario, String evento) {
        return preferenciaRepository
                .findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(usuario.getIdUsuario(), evento)
                .map(p -> Boolean.TRUE.equals(p.getActiva()))
                .orElse(true);
    }

    public boolean canalActivo(Usuario usuario, String canal) {
        return preferenciaRepository
                .findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(usuario.getIdUsuario(), canal)
                .map(p -> Boolean.TRUE.equals(p.getActiva()))
                .orElse(true);
    }
}
