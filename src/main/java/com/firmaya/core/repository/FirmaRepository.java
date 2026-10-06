package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Firma;

public interface FirmaRepository extends JpaRepository<Firma, Integer> {

    List<Firma> findByParteContratoIdContrato(Integer idContrato);

    Optional<Firma> findFirstByParteIdParteOrderByIdFirmaDesc(Integer idParte);

    Optional<Firma> findByIdFirmaAndParteContratoIdContrato(Integer idFirma, Integer idContrato);

    Optional<Firma> findByTokenFirma(String tokenFirma);
}
