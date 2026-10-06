package com.firmaya.core.entity;

import java.time.LocalDate;
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
@Table(name = "firma")
public class Firma {

    public static final String ESTADO_PENDIENTE = "Pendiente";
    public static final String ESTADO_NOTIFICADO = "Notificado";
    public static final String ESTADO_RENOTIFICADO = "Re-notificado";
    public static final String ESTADO_FIRMADO = "Firmado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_firma")
    private Integer idFirma;

    @ManyToOne
    @JoinColumn(name = "id_parte")
    private UsuarioContrato parte;

    @ManyToOne
    @JoinColumn(name = "id_version")
    private VersionContrato version;

    @Column(name = "estado_firma", length = 30)
    private String estadoFirma;

    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;

    @Column(name = "hash_firma", length = 255)
    private String hashFirma;

    @Column(name = "token_firma", length = 255)
    private String tokenFirma;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "intentos_otp_fallidos")
    private Integer intentosOtpFallidos;

    @Column(name = "enlace_bloqueado")
    private Boolean enlaceBloqueado;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "fecha_ultimo_evento")
    private LocalDateTime fechaUltimoEvento;

    public Integer getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(Integer idFirma) {
        this.idFirma = idFirma;
    }

    public UsuarioContrato getParte() {
        return parte;
    }

    public void setParte(UsuarioContrato parte) {
        this.parte = parte;
    }

    public VersionContrato getVersion() {
        return version;
    }

    public void setVersion(VersionContrato version) {
        this.version = version;
    }

    public String getEstadoFirma() {
        return estadoFirma;
    }

    public void setEstadoFirma(String estadoFirma) {
        this.estadoFirma = estadoFirma;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    public String getHashFirma() {
        return hashFirma;
    }

    public void setHashFirma(String hashFirma) {
        this.hashFirma = hashFirma;
    }

    public String getTokenFirma() {
        return tokenFirma;
    }

    public void setTokenFirma(String tokenFirma) {
        this.tokenFirma = tokenFirma;
    }

    public LocalDateTime getFechaExpiracionToken() {
        return fechaExpiracionToken;
    }

    public void setFechaExpiracionToken(LocalDateTime fechaExpiracionToken) {
        this.fechaExpiracionToken = fechaExpiracionToken;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public Integer getIntentosOtpFallidos() {
        return intentosOtpFallidos;
    }

    public void setIntentosOtpFallidos(Integer intentosOtpFallidos) {
        this.intentosOtpFallidos = intentosOtpFallidos;
    }

    public Boolean getEnlaceBloqueado() {
        return enlaceBloqueado;
    }

    public void setEnlaceBloqueado(Boolean enlaceBloqueado) {
        this.enlaceBloqueado = enlaceBloqueado;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDateTime getFechaUltimoEvento() {
        return fechaUltimoEvento;
    }

    public void setFechaUltimoEvento(LocalDateTime fechaUltimoEvento) {
        this.fechaUltimoEvento = fechaUltimoEvento;
    }
}
