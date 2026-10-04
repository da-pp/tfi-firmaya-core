package com.firmaya.core.dto;

/**
 * Archivo PDF a descargar (CU-10).
 */
public class ArchivoPdf {

    private final String nombreArchivo;
    private final byte[] contenido;

    public ArchivoPdf(String nombreArchivo, byte[] contenido) {
        this.nombreArchivo = nombreArchivo;
        this.contenido = contenido;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public byte[] getContenido() {
        return contenido;
    }
}
