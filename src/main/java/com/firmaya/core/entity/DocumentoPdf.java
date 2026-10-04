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

/**
 * PDF final del contrato firmado (CU-10).
 */
@Entity
@Table(name = "documento_pdf")
public class DocumentoPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pdf")
    private Integer idPdf;

    @ManyToOne
    @JoinColumn(name = "id_version")
    private VersionContrato version;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    // BYTEA en PostgreSQL
    @Column(name = "contenido_final")
    private byte[] contenidoFinal;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    // SHA-256 del archivo PDF
    @Column(name = "hash_documento", length = 64)
    private String hashDocumento;

    public Integer getIdPdf() {
        return idPdf;
    }

    public void setIdPdf(Integer idPdf) {
        this.idPdf = idPdf;
    }

    public VersionContrato getVersion() {
        return version;
    }

    public void setVersion(VersionContrato version) {
        this.version = version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public byte[] getContenidoFinal() {
        return contenidoFinal;
    }

    public void setContenidoFinal(byte[] contenidoFinal) {
        this.contenidoFinal = contenidoFinal;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getHashDocumento() {
        return hashDocumento;
    }

    public void setHashDocumento(String hashDocumento) {
        this.hashDocumento = hashDocumento;
    }
}
