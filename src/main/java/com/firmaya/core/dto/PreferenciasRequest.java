package com.firmaya.core.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

/**
 * CU-20 pasos 5 a 11.
 */
public class PreferenciasRequest {

    @Valid
    private List<OpcionPreferencia> eventos = new ArrayList<>();

    @Valid
    private List<OpcionPreferencia> canales = new ArrayList<>();

    // Camino alternativo: el usuario confirmó guardar con todos los eventos desactivados
    private boolean confirmarTodoDesactivado;

    public List<OpcionPreferencia> getEventos() {
        return eventos;
    }

    public void setEventos(List<OpcionPreferencia> eventos) {
        this.eventos = eventos;
    }

    public List<OpcionPreferencia> getCanales() {
        return canales;
    }

    public void setCanales(List<OpcionPreferencia> canales) {
        this.canales = canales;
    }

    public boolean isConfirmarTodoDesactivado() {
        return confirmarTodoDesactivado;
    }

    public void setConfirmarTodoDesactivado(boolean confirmarTodoDesactivado) {
        this.confirmarTodoDesactivado = confirmarTodoDesactivado;
    }
}
