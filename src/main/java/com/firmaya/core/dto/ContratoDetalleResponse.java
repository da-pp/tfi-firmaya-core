package com.firmaya.core.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contrato con los datos de su versión actual (CU-02 pasos 3 a 8 y cabecera de las pantallas del contrato).
 */
public class ContratoDetalleResponse {

    private Integer idContrato;
    private String nombre;
    private String estado;
    private String partesInvolucradas;
    private LocalDate fechaInicio;
    private LocalDate fechaExpiracion;
    private String descripcionPropiedad;
    private Integer idPlantilla;
    private Integer numeroVersion;
    private String hash;
    private String contenido;
    private LocalDateTime fechaUltimaModificacion;
    private boolean editable;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPartesInvolucradas() {
        return partesInvolucradas;
    }

    public void setPartesInvolucradas(String partesInvolucradas) {
        this.partesInvolucradas = partesInvolucradas;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDate fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getDescripcionPropiedad() {
        return descripcionPropiedad;
    }

    public void setDescripcionPropiedad(String descripcionPropiedad) {
        this.descripcionPropiedad = descripcionPropiedad;
    }

    public Integer getIdPlantilla() {
        return idPlantilla;
    }

    public void setIdPlantilla(Integer idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
