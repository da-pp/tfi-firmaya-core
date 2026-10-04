package com.firmaya.core.dto;

/**
 * CU-12 paso 11: fragmento de texto de la comparación.
 * AGREGADO se resalta en verde (Versión B) y ELIMINADO en rojo (Versión A).
 */
public class FragmentoComparacion {

    public static final String IGUAL = "IGUAL";
    public static final String AGREGADO = "AGREGADO";
    public static final String ELIMINADO = "ELIMINADO";

    private String tipo;
    private String texto;

    public FragmentoComparacion(String tipo, String texto) {
        this.tipo = tipo;
        this.texto = texto;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
