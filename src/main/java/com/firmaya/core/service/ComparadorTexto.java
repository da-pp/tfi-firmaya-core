package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.firmaya.core.dto.FragmentoComparacion;

/**
 * CU-12: diferencias por palabras entre dos textos.
 *
 * El texto se divide en palabras y espacios, y se calcula la subsecuencia común más larga (LCS).
 * Lo que solo está en A es ELIMINADO, lo que solo está en B es AGREGADO y lo común es IGUAL.
 * Cada bloque contiguo de diferencias cuenta como un cambio.
 */
public final class ComparadorTexto {

    private static final Pattern PATRON_TOKEN = Pattern.compile("\\S+|\\s+");

    private ComparadorTexto() {
    }

    public static List<FragmentoComparacion> comparar(String textoA, String textoB) {
        List<String> a = dividir(textoA);
        List<String> b = dividir(textoB);

        // El inicio y el final en común no necesitan la tabla LCS
        int inicio = 0;
        while (inicio < a.size() && inicio < b.size() && a.get(inicio).equals(b.get(inicio))) {
            inicio++;
        }
        int finA = a.size();
        int finB = b.size();
        while (finA > inicio && finB > inicio && a.get(finA - 1).equals(b.get(finB - 1))) {
            finA--;
            finB--;
        }

        List<FragmentoComparacion> fragmentos = new ArrayList<>();
        agregar(fragmentos, FragmentoComparacion.IGUAL, unir(a, 0, inicio));
        compararMedio(fragmentos, a.subList(inicio, finA), b.subList(inicio, finB));
        agregar(fragmentos, FragmentoComparacion.IGUAL, unir(a, finA, a.size()));
        return fragmentos;
    }

    public static int contarCambios(List<FragmentoComparacion> fragmentos) {
        int cambios = 0;
        boolean dentroDeCambio = false;
        for (FragmentoComparacion fragmento : fragmentos) {
            boolean esCambio = !FragmentoComparacion.IGUAL.equals(fragmento.getTipo());
            if (esCambio && !dentroDeCambio) {
                cambios++;
            }
            dentroDeCambio = esCambio;
        }
        return cambios;
    }

    private static void compararMedio(List<FragmentoComparacion> fragmentos, List<String> a, List<String> b) {
        // lcs[i][j] = largo de la subsecuencia común más larga entre a[i..] y b[j..]
        int[][] lcs = new int[a.size() + 1][b.size() + 1];
        for (int i = a.size() - 1; i >= 0; i--) {
            for (int j = b.size() - 1; j >= 0; j--) {
                if (a.get(i).equals(b.get(j))) {
                    lcs[i][j] = lcs[i + 1][j + 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }

        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).equals(b.get(j))) {
                agregar(fragmentos, FragmentoComparacion.IGUAL, a.get(i));
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                agregar(fragmentos, FragmentoComparacion.ELIMINADO, a.get(i));
                i++;
            } else {
                agregar(fragmentos, FragmentoComparacion.AGREGADO, b.get(j));
                j++;
            }
        }
        while (i < a.size()) {
            agregar(fragmentos, FragmentoComparacion.ELIMINADO, a.get(i));
            i++;
        }
        while (j < b.size()) {
            agregar(fragmentos, FragmentoComparacion.AGREGADO, b.get(j));
            j++;
        }
    }

    // Une el texto al último fragmento si es del mismo tipo
    private static void agregar(List<FragmentoComparacion> fragmentos, String tipo, String texto) {
        if (texto.isEmpty()) {
            return;
        }
        if (!fragmentos.isEmpty()) {
            FragmentoComparacion ultimo = fragmentos.get(fragmentos.size() - 1);
            if (ultimo.getTipo().equals(tipo)) {
                ultimo.setTexto(ultimo.getTexto() + texto);
                return;
            }
        }
        fragmentos.add(new FragmentoComparacion(tipo, texto));
    }

    private static List<String> dividir(String texto) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = PATRON_TOKEN.matcher(texto == null ? "" : texto);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static String unir(List<String> tokens, int desde, int hasta) {
        StringBuilder sb = new StringBuilder();
        for (int i = desde; i < hasta; i++) {
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }
}
