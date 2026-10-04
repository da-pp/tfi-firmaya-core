package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * CU-08 pasos 12 y 14: código OTP de exactamente 6 dígitos numéricos.
 */
public class ConfirmarFirmaRequest {

    @NotBlank(message = "El campo Código OTP es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe tener exactamente 6 dígitos numéricos")
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
