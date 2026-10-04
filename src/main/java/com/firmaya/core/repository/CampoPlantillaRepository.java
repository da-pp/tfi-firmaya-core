package com.firmaya.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.CampoPlantilla;

public interface CampoPlantillaRepository extends JpaRepository<CampoPlantilla, Integer> {

    List<CampoPlantilla> findByPlantillaIdPlantillaOrderByIdCampo(Integer idPlantilla);

    void deleteByPlantillaIdPlantilla(Integer idPlantilla);
}
