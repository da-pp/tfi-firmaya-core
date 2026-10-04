package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-11 paso 2 y CU-14 paso 2: historial de versiones del contrato.
 */
public class HistorialVersionesResponse {

    private List<VersionResponse> versiones;
    // "Este contrato aún no tiene versiones anteriores." cuando solo existe la versión 1
    private String mensaje;
    // CU-14: falso si el contrato está Firmado o Archivado
    private boolean restauracionDisponible;
    private String mensajeRestauracion;

    public List<VersionResponse> getVersiones() {
        return versiones;
    }

    public void setVersiones(List<VersionResponse> versiones) {
        this.versiones = versiones;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isRestauracionDisponible() {
        return restauracionDisponible;
    }

    public void setRestauracionDisponible(boolean restauracionDisponible) {
        this.restauracionDisponible = restauracionDisponible;
    }

    public String getMensajeRestauracion() {
        return mensajeRestauracion;
    }

    public void setMensajeRestauracion(String mensajeRestauracion) {
        this.mensajeRestauracion = mensajeRestauracion;
    }
}
