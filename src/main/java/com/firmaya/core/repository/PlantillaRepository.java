package com.firmaya.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Plantilla;

public interface PlantillaRepository extends JpaRepository<Plantilla, Integer> {

    List<Plantilla> findByEstadoOrderByTipoContratoAscNombreAsc(String estado);
}
