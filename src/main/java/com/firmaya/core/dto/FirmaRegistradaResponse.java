package com.firmaya.core.dto;

/**
 * CU-08 pasos 19 a 21.
 */
public class FirmaRegistradaResponse {

    private Integer idContrato;
    // Verdadero si con esta firma firmaron todos y el contrato pasó a "Firmado"
    private boolean contratoFirmado;
    private String mensaje;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public boolean isContratoFirmado() {
        return contratoFirmado;
    }

    public void setContratoFirmado(boolean contratoFirmado) {
        this.contratoFirmado = contratoFirmado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
