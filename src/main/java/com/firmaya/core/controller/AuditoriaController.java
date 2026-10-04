package com.firmaya.core.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.dto.AuditoriaDetalleResponse;
import com.firmaya.core.dto.AuditoriaPaginaResponse;
import com.firmaya.core.service.AuditoriaConsultaService;

/**
 * CU-18 – Registro de acciones de auditoría. Fechas en formato DD/MM/AAAA.
 */
@RestController
@RequestMapping("/api/admin/auditoria")
public class AuditoriaController {

    private final AuditoriaConsultaService auditoriaConsultaService;

    public AuditoriaController(AuditoriaConsultaService auditoriaConsultaService) {
        this.auditoriaConsultaService = auditoriaConsultaService;
    }

    @GetMapping
    public AuditoriaPaginaResponse buscar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String tipoAccion,
            @RequestParam(required = false) String contrato,
            @RequestParam(defaultValue = "1") int pagina) {
        return auditoriaConsultaService.buscar(fechaDesde, fechaHasta, usuario, tipoAccion, contrato, pagina);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String tipoAccion,
            @RequestParam(required = false) String contrato) {
        String csv = auditoriaConsultaService.exportarCsv(fechaDesde, fechaHasta, usuario, tipoAccion, contrato);
        // BOM UTF-8 para que los acentos se vean bien al abrir el archivo en Excel
        byte[] contenido = ("﻿" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"registro_auditoria.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenido);
    }

    @GetMapping("/{idRegistro}")
    public AuditoriaDetalleResponse detalle(@PathVariable Integer idRegistro) {
        return auditoriaConsultaService.obtenerDetalle(idRegistro);
    }
}
