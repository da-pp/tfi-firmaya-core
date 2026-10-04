package com.firmaya.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.EstadoContrato;

public interface EstadoContratoRepository extends JpaRepository<EstadoContrato, Integer> {

    Optional<EstadoContrato> findByNombre(String nombre);
}
