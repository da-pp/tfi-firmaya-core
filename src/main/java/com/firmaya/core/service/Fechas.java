package com.firmaya.core.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import com.firmaya.core.exception.ReglaNegocioException;

public final class Fechas {

    public static final DateTimeFormatter DD_MM_AAAA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Fechas() {
    }

    public static LocalDate leer(String valor, String campo, String mensajeError) {
        try {
            return LocalDate.parse(valor.trim(), DD_MM_AAAA);
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(campo, mensajeError);
        }
    }

    public static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(DD_MM_AAAA);
    }
}
