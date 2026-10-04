package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.firmaya.core.entity.VersionContrato;

public interface VersionContratoRepository extends JpaRepository<VersionContrato, Integer> {

    // Versión actual: la de mayor número
    Optional<VersionContrato> findTopByContratoIdContratoOrderByNumeroVersionDesc(Integer idContrato);

    // Historial de la más reciente a la más antigua (CU-11)
    List<VersionContrato> findByContratoIdContratoOrderByNumeroVersionDesc(Integer idContrato);

    Optional<VersionContrato> findByContratoIdContratoAndNumeroVersion(Integer idContrato, Integer numeroVersion);

    // Última modificación de cada contrato: [idContrato, fecha de la última versión] (CU-17)
    @Query("select v.contrato.idContrato, max(v.fechaCreacion) from VersionContrato v group by v.contrato.idContrato")
    List<Object[]> buscarUltimaModificacionPorContrato();
}
