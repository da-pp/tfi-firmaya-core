package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-16 pasos 5 a 9: editor de plantillas.
 */
public class PlantillaRequest {

    @NotBlank(message = "El campo Nombre de la plantilla es obligatorio")
    @Size(max = 200, message = "El nombre de la plantilla no puede superar los 200 caracteres")
    private String nombre;

    @NotBlank(message = "El campo Tipo de contrato es obligatorio")
    @Pattern(regexp = "^(Arrendamiento|Venta|Mandato|Otro)$",
            message = "El tipo de contrato debe ser Arrendamiento, Venta, Mandato u Otro")
    private String tipoContrato;

    @Size(max = 500, message = "La descripción de uso no puede superar los 500 caracteres")
    private String descripcionUso;

    @NotBlank(message = "El campo Plantilla es obligatorio")
    private String cuerpo;

    @NotBlank(message = "El campo Estado es obligatorio")
    @Pattern(regexp = "^(Activa|Inactiva)$", message = "El estado debe ser Activa o Inactiva")
    private String estado;

    // Camino alternativo: el administrador confirmó "Guardar sin campos"
    private boolean guardarSinCampos;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getDescripcionUso() {
        return descripcionUso;
    }

    public void setDescripcionUso(String descripcionUso) {
        this.descripcionUso = descripcionUso;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isGuardarSinCampos() {
        return guardarSinCampos;
    }

    public void setGuardarSinCampos(boolean guardarSinCampos) {
        this.guardarSinCampos = guardarSinCampos;
    }
}
