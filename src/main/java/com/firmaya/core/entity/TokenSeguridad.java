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
@Table(name = "token_seguridad")
public class TokenSeguridad {

    public static final String TIPO_RECUPERACION = "RECUPERACION";
    public static final String TIPO_ACTIVACION = "ACTIVACION";
    public static final String TIPO_OTP = "OTP";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Parte del contrato dueña del OTP cuando el firmante es externo (CU-08, ampliación I6)
    @Column(name = "id_parte")
    private Integer idParte;

    @Column(name = "tipo_token", length = 20)
    private String tipoToken;

    @Column(name = "valor", length = 255)
    private String valor;

    @Column(name = "usado")
    private Boolean usado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    public Integer getIdToken() {
        return idToken;
    }

    public void setIdToken(Integer idToken) {
        this.idToken = idToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
}
