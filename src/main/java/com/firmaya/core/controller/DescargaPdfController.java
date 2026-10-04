package com.firmaya.core.controller;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.ArchivoPdf;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.DescargaPdfService;

/**
 * CU-10 – Descargar contrato firmado en PDF.
 * Usuarios internos con sesión; partes externas con el enlace de invitación o de firma.
 */
@RestController
public class DescargaPdfController {

    private final DescargaPdfService descargaPdfService;

    public DescargaPdfController(DescargaPdfService descargaPdfService) {
        this.descargaPdfService = descargaPdfService;
    }

    @GetMapping("/api/contratos/{idContrato}/pdf")
    public ResponseEntity<byte[]> descargarFirmado(@PathVariable Integer idContrato,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return respuesta(descargaPdfService.descargarFirmado(idContrato, usuario, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/contratos/{idContrato}/pdf/sin-firmas")
    public ResponseEntity<byte[]> descargarSinFirmas(@PathVariable Integer idContrato,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return respuesta(descargaPdfService.descargarSinFirmas(idContrato, usuario, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/externo/acceso/{token}/pdf")
    public ResponseEntity<byte[]> descargarFirmadoExterno(@PathVariable String token, HttpServletRequest httpRequest) {
        return respuesta(descargaPdfService.descargarFirmadoExterno(token, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/externo/acceso/{token}/pdf/sin-firmas")
    public ResponseEntity<byte[]> descargarSinFirmasExterno(@PathVariable String token,
            HttpServletRequest httpRequest) {
        return respuesta(descargaPdfService.descargarSinFirmasExterno(token, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/externo/firma/{token}/pdf")
    public ResponseEntity<byte[]> descargarConEnlaceDeFirma(@PathVariable String token,
            HttpServletRequest httpRequest) {
        return respuesta(descargaPdfService.descargarFirmadoConEnlaceDeFirma(token, httpRequest.getRemoteAddr()));
    }

    private ResponseEntity<byte[]> respuesta(ArchivoPdf archivo) {
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.getNombreArchivo(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo.getContenido());
    }
}
