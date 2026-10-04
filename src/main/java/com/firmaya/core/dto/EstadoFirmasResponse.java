package com.firmaya.core.dto;

import java.util.List;

/**
 * Firmantes con el progreso "X de Y firmas completadas" (CU-07 paso 18, CU-09 pasos 2 a 19).
 */
public class EstadoFirmasResponse {

    private List<FirmanteResponse> firmantes;
    private int firmasCompletadas;
    private int totalFirmantes;
    private String progreso;
    private boolean todasCompletadas;
    private String mensaje;

    public List<FirmanteResponse> getFirmantes() {
        return firmantes;
    }

    public void setFirmantes(List<FirmanteResponse> firmantes) {
        this.firmantes = firmantes;
    }

    public int getFirmasCompletadas() {
        return firmasCompletadas;
    }

    public void setFirmasCompletadas(int firmasCompletadas) {
        this.firmasCompletadas = firmasCompletadas;
    }

    public int getTotalFirmantes() {
        return totalFirmantes;
    }

    public void setTotalFirmantes(int totalFirmantes) {
        this.totalFirmantes = totalFirmantes;
    }

    public String getProgreso() {
        return progreso;
    }

    public void setProgreso(String progreso) {
        this.progreso = progreso;
    }

    public boolean isTodasCompletadas() {
        return todasCompletadas;
    }

    public void setTodasCompletadas(boolean todasCompletadas) {
        this.todasCompletadas = todasCompletadas;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
