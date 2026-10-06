package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.firmaya.core.entity.VersionContrato;

public interface VersionContratoRepository extends JpaRepository<VersionContrato, Integer> {

    Optional<VersionContrato> findTopByContratoIdContratoOrderByNumeroVersionDesc(Integer idContrato);

    List<VersionContrato> findByContratoIdContratoOrderByNumeroVersionDesc(Integer idContrato);

    Optional<VersionContrato> findByContratoIdContratoAndNumeroVersion(Integer idContrato, Integer numeroVersion);

    @Query("select v.contrato.idContrato, max(v.fechaCreacion) from VersionContrato v group by v.contrato.idContrato")
    List<Object[]> buscarUltimaModificacionPorContrato();
}
