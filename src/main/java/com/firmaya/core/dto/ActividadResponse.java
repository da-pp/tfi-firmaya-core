package com.firmaya.core.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * CU-17 pasos 2 a 9 y 15: métricas, gráfico por estado y contratos más recientes del período.
 */
public class ActividadResponse {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long contratosActivos;
    private long contratosPendientesFirma;
    private long contratosFirmados;
    private long contratosProximosAVencer;
    private List<ConteoEstadoResponse> contratosPorEstado;
    private List<ContratoResumenResponse> contratosRecientes;
    private String mensaje;

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public long getContratosActivos() {
        return contratosActivos;
    }

    public void setContratosActivos(long contratosActivos) {
        this.contratosActivos = contratosActivos;
    }

    public long getContratosPendientesFirma() {
        return contratosPendientesFirma;
    }

    public void setContratosPendientesFirma(long contratosPendientesFirma) {
        this.contratosPendientesFirma = contratosPendientesFirma;
    }

    public long getContratosFirmados() {
        return contratosFirmados;
    }

    public void setContratosFirmados(long contratosFirmados) {
        this.contratosFirmados = contratosFirmados;
    }

    public long getContratosProximosAVencer() {
        return contratosProximosAVencer;
    }

    public void setContratosProximosAVencer(long contratosProximosAVencer) {
        this.contratosProximosAVencer = contratosProximosAVencer;
    }

    public List<ConteoEstadoResponse> getContratosPorEstado() {
        return contratosPorEstado;
    }

    public void setContratosPorEstado(List<ConteoEstadoResponse> contratosPorEstado) {
        this.contratosPorEstado = contratosPorEstado;
    }

    public List<ContratoResumenResponse> getContratosRecientes() {
        return contratosRecientes;
    }

    public void setContratosRecientes(List<ContratoResumenResponse> contratosRecientes) {
        this.contratosRecientes = contratosRecientes;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
