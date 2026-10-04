package com.firmaya.core.service;

/**
 * El contenido de los contratos es HTML (editor enriquecido). Esta utilidad obtiene su texto.
 */
public final class ContenidoHtml {

    private ContenidoHtml() {
    }

    /**
     * Texto sin etiquetas. Los fines de párrafo, títulos, ítems y saltos de línea se convierten en "\n".
     */
    public static String textoPlano(String html) {
        if (html == null) {
            return "";
        }
        String texto = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|h[1-6]|blockquote)>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&");
        return texto.trim();
    }

    /**
     * Escapa un texto para insertarlo dentro del contenido HTML.
     */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
