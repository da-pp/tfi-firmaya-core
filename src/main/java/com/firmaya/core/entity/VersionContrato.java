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
@Table(name = "version_contrato")
public class VersionContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_version")
    private Integer idVersion;

    @ManyToOne
    @JoinColumn(name = "id_contrato")
    private Contrato contrato;

    @ManyToOne
    @JoinColumn(name = "id_usuario_autor")
    private Usuario usuarioAutor;

    @Column(name = "numero_version")
    private Integer numeroVersion;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    // CU-02/11/14 (ampliación I2)
    @Column(name = "comentario", length = 500)
    private String comentario;

    // CU-14 (ampliación I2)
    @Column(name = "razon_restauracion", length = 500)
    private String razonRestauracion;

    public Integer getIdVersion() {
        return idVersion;
    }

    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Usuario getUsuarioAutor() {
        return usuarioAutor;
    }

    public void setUsuarioAutor(Usuario usuarioAutor) {
        this.usuarioAutor = usuarioAutor;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
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
}
