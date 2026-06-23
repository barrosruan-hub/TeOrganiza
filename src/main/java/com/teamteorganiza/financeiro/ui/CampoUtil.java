package com.teamteorganiza.financeiro.ui;

/** Conversões simples de texto dos formulários do financeiro. */
final class CampoUtil {

    private CampoUtil() {}

    static double valor(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    static int id(String texto) {
        return Integer.parseInt(texto.trim());
    }
}
