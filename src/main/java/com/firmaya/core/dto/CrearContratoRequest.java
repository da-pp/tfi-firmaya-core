package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CrearContratoRequest {

    public static final String MENSAJE_FECHA_INICIO =
            "La fecha debe tener el formato DD/MM/AAAA y no puede ser anterior a la fecha de hoy";

    @NotNull(message = "Este campo es obligatorio")
    private Integer idPlantilla;

    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 200, message = "El nombre del contrato no puede superar los 200 caracteres")
    private String nombre;

    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 1000, message = "Las partes involucradas no pueden superar los 1000 caracteres")
    private String partesInvolucradas;

    @NotBlank(message = "Este campo es obligatorio")
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = MENSAJE_FECHA_INICIO)
    private String fechaInicio;

    @Pattern(regexp = "^(\\d{2}/\\d{2}/\\d{4})?$", message = "La fecha debe tener el formato DD/MM/AAAA")
    private String fechaExpiracion;

    @Size(max = 2000, message = "La descripción de la propiedad no puede superar los 2000 caracteres")
    private String descripcionPropiedad;

    public Integer getIdPlantilla() {
        return idPlantilla;
    }

    public void setIdPlantilla(Integer idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPartesInvolucradas() {
        return partesInvolucradas;
    }

    public void setPartesInvolucradas(String partesInvolucradas) {
        this.partesInvolucradas = partesInvolucradas;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getDescripcionPropiedad() {
        return descripcionPropiedad;
    }

    public void setDescripcionPropiedad(String descripcionPropiedad) {
        this.descripcionPropiedad = descripcionPropiedad;
    }
}
