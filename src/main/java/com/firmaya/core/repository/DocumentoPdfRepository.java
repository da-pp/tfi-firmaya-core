package com.firmaya.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.DocumentoPdf;

public interface DocumentoPdfRepository extends JpaRepository<DocumentoPdf, Integer> {

    Optional<DocumentoPdf> findFirstByVersionIdVersionOrderByIdPdfDesc(Integer idVersion);
}
