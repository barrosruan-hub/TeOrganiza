package com.teamteorganiza.financeiro.ui;

final class CampoUtil {

    private CampoUtil() {}

    static double valor(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    static String id(String texto) {
        String s = texto.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("ID vazio");
        return s;
    }
}
