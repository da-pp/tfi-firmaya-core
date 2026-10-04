package com.firmaya.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.firmaya.core.entity.Auditoria;

// JpaSpecificationExecutor permite combinar los filtros opcionales del CU-18
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer>, JpaSpecificationExecutor<Auditoria> {
}
