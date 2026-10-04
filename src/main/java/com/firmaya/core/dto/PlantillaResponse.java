package com.firmaya.core.dto;

import java.util.List;

/**
 * Datos de una plantilla (CU-16 lista y detalle, CU-01 selección de plantilla).
 * En la lista del CU-16 no se envían cuerpo ni campos.
 */
public class PlantillaResponse {

    private Integer idPlantilla;
    private String nombre;
    private String tipoContrato;
    private String descripcionUso;
    private String cuerpo;
    private String estado;
    private Integer version;
    private List<String> camposDinamicos;
    private Boolean tieneContratosActivos;
    // Solo al crear o editar: "Plantilla guardada exitosamente."
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getIdPlantilla() {
        return idPlantilla;
    }

    public void setIdPlantilla(Integer idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getDescripcionUso() {
        return descripcionUso;
    }

    public void setDescripcionUso(String descripcionUso) {
        this.descripcionUso = descripcionUso;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<String> getCamposDinamicos() {
        return camposDinamicos;
    }

    public void setCamposDinamicos(List<String> camposDinamicos) {
        this.camposDinamicos = camposDinamicos;
    }

    public Boolean getTieneContratosActivos() {
        return tieneContratosActivos;
    }

    public void setTieneContratosActivos(Boolean tieneContratosActivos) {
        this.tieneContratosActivos = tieneContratosActivos;
    }
}
