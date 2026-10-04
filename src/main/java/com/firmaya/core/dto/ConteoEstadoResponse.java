package com.firmaya.core.dto;

/**
 * CU-17 paso 8: cantidad de contratos de un estado (gráfico de contratos por estado).
 */
public class ConteoEstadoResponse {

    private String estado;
    private long cantidad;

    public ConteoEstadoResponse(String estado, long cantidad) {
        this.estado = estado;
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public long getCantidad() {
        return cantidad;
    }
}
