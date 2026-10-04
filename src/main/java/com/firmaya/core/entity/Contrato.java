package com.firmaya.core.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    @ManyToOne
    @JoinColumn(name = "id_estado")
    private EstadoContrato estado;

    @ManyToOne
    @JoinColumn(name = "id_plantilla")
    private Plantilla plantilla;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    // Longitud 200 según CU-01 (ampliación I1)
    @Column(name = "nombre", length = 200)
    private String nombre;

    // Longitud 2000 según CU-01 (ampliación I1)
    @Column(name = "descripcion_propiedad", length = 2000)
    private String descripcionPropiedad;

    // CU-01 (ampliación I1)
    @Column(name = "partes_involucradas", length = 1000)
    private String partesInvolucradas;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_expiracion")
    private LocalDate fechaExpiracion;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public EstadoContrato getEstado() {
        return estado;
    }

    public void setEstado(EstadoContrato estado) {
        this.estado = estado;
    }

    public Plantilla getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(Plantilla plantilla) {
        this.plantilla = plantilla;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcionPropiedad() {
        return descripcionPropiedad;
    }

    public void setDescripcionPropiedad(String descripcionPropiedad) {
        this.descripcionPropiedad = descripcionPropiedad;
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
}
