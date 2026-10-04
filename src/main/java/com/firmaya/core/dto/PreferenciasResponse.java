package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-20 pasos 4 a 7, 14 y 16: estado actual de eventos y canales.
 */
public class PreferenciasResponse {

    private List<OpcionPreferencia> eventos;
    private List<OpcionPreferencia> canales;
    private String mensaje;

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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
