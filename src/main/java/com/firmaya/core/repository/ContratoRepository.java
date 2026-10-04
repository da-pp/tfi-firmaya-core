package com.firmaya.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.firmaya.core.entity.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    List<Contrato> findByNombreContainingIgnoreCase(String nombre);

    List<Contrato> findByUsuarioCreadorIdUsuario(Integer idUsuario);

    // Contratos activos (no Archivados) creados a partir de una plantilla (CU-16)
    boolean existsByPlantillaIdPlantillaAndEstadoNombreNot(Integer idPlantilla, String estado);

    // Última firma de cada contrato: [idContrato, fecha de la última firma] (CU-17)
    @Query(value = "select uc.id_contrato, max(f.fecha_firma) from firma f "
            + "join usuario_contrato uc on uc.id_parte = f.id_parte "
            + "where f.fecha_firma is not null group by uc.id_contrato", nativeQuery = true)
    List<Object[]> buscarUltimaFirmaPorContrato();
}
