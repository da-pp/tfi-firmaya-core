package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.PreferenciaNotificacion;

public interface PreferenciaNotificacionRepository extends JpaRepository<PreferenciaNotificacion, Integer> {

    List<PreferenciaNotificacion> findByUsuarioIdUsuario(Integer idUsuario);

    Optional<PreferenciaNotificacion> findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(Integer idUsuario,
            String tipoEvento);

    Optional<PreferenciaNotificacion> findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(Integer idUsuario,
            String canal);

    void deleteByUsuarioIdUsuario(Integer idUsuario);
}
