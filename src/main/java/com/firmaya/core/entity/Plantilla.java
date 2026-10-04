package com.firmaya.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "plantilla")
public class Plantilla {

    public static final String ESTADO_ACTIVA = "Activa";
    public static final String ESTADO_INACTIVA = "Inactiva";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plantilla")
    private Integer idPlantilla;

    // Longitud 200 según CU-16 (ampliación I9)
    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "tipo_contrato", length = 50)
    private String tipoContrato;

    // Longitud 500 según CU-16 (ampliación I9)
    @Column(name = "descripcion_uso", length = 500)
    private String descripcionUso;

    @Column(name = "cuerpo", columnDefinition = "TEXT")
    private String cuerpo;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "version")
    private Integer version;

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
}
