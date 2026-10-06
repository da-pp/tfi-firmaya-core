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
@Table(name = "usuario_contrato")
public class UsuarioContrato {

    public static final String ROL_FIRMANTE = "Firmante";
    public static final String ROL_SOLO_LECTURA = "Solo lectura";
    public static final String ROL_REVISOR = "Revisor";

    public static final String INVITACION_PENDIENTE = "Pendiente";
    public static final String INVITACION_ENVIADA = "Invitación enviada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parte")
    private Integer idParte;

    @ManyToOne
    @JoinColumn(name = "id_contrato")
    private Contrato contrato;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "correo_invitado", length = 254)
    private String correoInvitado;

    @Column(name = "rol_parte", length = 50)
    private String rolParte;

    @Column(name = "estado_invitacion", length = 30)
    private String estadoInvitacion;

    @Column(name = "fecha_invitacion")
    private LocalDateTime fechaInvitacion;

    @Column(name = "token_invitacion", length = 255)
    private String tokenInvitacion;

    @Column(name = "nombre_parte", length = 150)
    private String nombreParte;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCorreoInvitado() {
        return correoInvitado;
    }

    public void setCorreoInvitado(String correoInvitado) {
        this.correoInvitado = correoInvitado;
    }

    public String getRolParte() {
        return rolParte;
    }

    public void setRolParte(String rolParte) {
        this.rolParte = rolParte;
    }

    public String getEstadoInvitacion() {
        return estadoInvitacion;
    }

    public void setEstadoInvitacion(String estadoInvitacion) {
        this.estadoInvitacion = estadoInvitacion;
    }

    public LocalDateTime getFechaInvitacion() {
        return fechaInvitacion;
    }

    public void setFechaInvitacion(LocalDateTime fechaInvitacion) {
        this.fechaInvitacion = fechaInvitacion;
    }

    public String getTokenInvitacion() {
        return tokenInvitacion;
    }

    public void setTokenInvitacion(String tokenInvitacion) {
        this.tokenInvitacion = tokenInvitacion;
    }

    public String getNombreParte() {
        return nombreParte;
    }

    public void setNombreParte(String nombreParte) {
        this.nombreParte = nombreParte;
    }

    public LocalDateTime getFechaExpiracionToken() {
        return fechaExpiracionToken;
    }

    public void setFechaExpiracionToken(LocalDateTime fechaExpiracionToken) {
        this.fechaExpiracionToken = fechaExpiracionToken;
    }
}
