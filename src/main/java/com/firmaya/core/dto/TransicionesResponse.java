package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-05 pasos 2 a 4: estado actual y estados disponibles según el flujo permitido.
 */
public class TransicionesResponse {

    private String estadoActual;
    private List<String> estadosDisponibles;

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    public void setEstadosDisponibles(List<String> estadosDisponibles) {
        this.estadosDisponibles = estadosDisponibles;
    }
}
