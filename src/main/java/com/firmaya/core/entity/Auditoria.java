package com.firmaya.core.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    // Nulo cuando la acción la realiza una parte externa (acceso por token)
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "tipo_accion", length = 50)
    private String tipoAccion;

    @Column(name = "entidad_afectada", length = 100)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada")
    private Integer idEntidadAfectada;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    // Detalle del registro en CU-18 (ampliación I11)
    @Column(name = "datos_antes", columnDefinition = "TEXT")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "TEXT")
    private String datosDespues;

    @Column(name = "id_version")
    private Integer idVersion;

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public Integer getIdEntidadAfectada() {
        return idEntidadAfectada;
    }

    public void setIdEntidadAfectada(Integer idEntidadAfectada) {
        this.idEntidadAfectada = idEntidadAfectada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDatosAntes() {
        return datosAntes;
    }

    public void setDatosAntes(String datosAntes) {
        this.datosAntes = datosAntes;
    }

    public String getDatosDespues() {
        return datosDespues;
    }

    public void setDatosDespues(String datosDespues) {
        this.datosDespues = datosDespues;
    }

    public Integer getIdVersion() {
        return idVersion;
    }

    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }
}
