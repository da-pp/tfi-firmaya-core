package com.firmaya.core.dto;

import java.util.List;

import javax.validation.constraints.NotEmpty;

/**
 * CU-07 camino alternativo "Fallo en el envío de notificaciones": firmas a reintentar.
 */
public class ReintentarFirmasRequest {

    @NotEmpty(message = "Debe indicar al menos un firmante")
    private List<Integer> idsFirma;

    public List<Integer> getIdsFirma() {
        return idsFirma;
    }

    public void setIdsFirma(List<Integer> idsFirma) {
        this.idsFirma = idsFirma;
    }
}
