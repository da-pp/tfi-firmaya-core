package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-07 pasos 8 a 10. La fecha límite se recibe con formato DD/MM/AAAA.
 */
public class SolicitarFirmasRequest {

    public static final String MENSAJE_FECHA_LIMITE = "La fecha límite debe ser posterior a la fecha actual";

    @NotBlank(message = "Debe seleccionar un canal de notificación")
    @Pattern(regexp = "^Correo electrónico$", message = "El canal de notificación disponible es Correo electrónico")
    private String canal;

    @Size(max = 500, message = "El mensaje personalizado no puede superar los 500 caracteres")
    private String mensaje;

    @Pattern(regexp = "^(\\d{2}/\\d{2}/\\d{4})?$", message = "La fecha debe tener el formato DD/MM/AAAA")
    private String fechaLimite;

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
