package com.firmaya.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.TransicionEstado;

public interface TransicionEstadoRepository extends JpaRepository<TransicionEstado, Integer> {

    List<TransicionEstado> findByEstadoOrigenIdEstado(Integer idEstadoOrigen);

    boolean existsByEstadoOrigenIdEstadoAndEstadoDestinoIdEstado(Integer idEstadoOrigen, Integer idEstadoDestino);
}
