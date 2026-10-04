package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.PreferenciaNotificacion;

public interface PreferenciaNotificacionRepository extends JpaRepository<PreferenciaNotificacion, Integer> {

    List<PreferenciaNotificacion> findByUsuarioIdUsuario(Integer idUsuario);

    // Fila de un evento (canal nulo)
    Optional<PreferenciaNotificacion> findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(Integer idUsuario,
            String tipoEvento);

    // Fila de un canal (evento nulo)
    Optional<PreferenciaNotificacion> findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(Integer idUsuario,
            String canal);

    void deleteByUsuarioIdUsuario(Integer idUsuario);
}
