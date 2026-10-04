package com.firmaya.core.dto;

import java.time.LocalDateTime;

/**
 * Una versión del contrato (CU-11 pasos 4 a 9, CU-12 paso 12, CU-14 pasos 5 a 7).
 * El contenido solo se envía al ver una versión.
 */
public class VersionResponse {

    private Integer numeroVersion;
    private String autor;
    private LocalDateTime fechaCreacion;
    private String comentario;
    private String razonRestauracion;
    private String hash;
    private boolean actual;
    private String contenido;

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getRazonRestauracion() {
        return razonRestauracion;
    }

    public void setRazonRestauracion(String razonRestauracion) {
        this.razonRestauracion = razonRestauracion;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
