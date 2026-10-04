package com.firmaya.core.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.dto.ActividadResponse;
import com.firmaya.core.dto.ContratosPaginaResponse;
import com.firmaya.core.service.ActividadService;

/**
 * CU-17 – Ver Panel de Actividad Global. Fechas en formato DD/MM/AAAA.
 * Métricas: ACTIVOS, PENDIENTES_FIRMA, FIRMADOS, PROXIMOS_A_VENCER.
 */
@RestController
@RequestMapping("/api/admin/actividad")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    public ActividadResponse obtenerActividad(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @RequestParam(required = false) List<String> estados) {
        return actividadService.obtenerActividad(fechaInicio, fechaFin, estados);
    }

    @GetMapping("/contratos")
    public ContratosPaginaResponse listarContratos(
            @RequestParam String metrica,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @RequestParam(required = false) List<String> estados,
            @RequestParam(defaultValue = "1") int pagina) {
        return actividadService.listarContratosDeMetrica(metrica, fechaInicio, fechaFin, estados, pagina);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String metrica,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaFin,
            @RequestParam(required = false) List<String> estados) {
        String csv = actividadService.exportarCsv(metrica, fechaInicio, fechaFin, estados);
        // BOM UTF-8 para que los acentos se vean bien al abrir el archivo en Excel
        byte[] contenido = ("﻿" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"panel_actividad.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenido);
    }
}
