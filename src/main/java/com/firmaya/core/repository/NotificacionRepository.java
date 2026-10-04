package com.firmaya.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Último correo de un tipo enviado a una dirección por un contrato (para "Reintentar")
    Optional<Notificacion> findTopByIdContratoAndCorreoDestinoAndTipoOrderByIdNotificacionDesc(Integer idContrato,
            String correoDestino, String tipo);
}
