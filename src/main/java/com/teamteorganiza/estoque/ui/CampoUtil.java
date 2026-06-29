package com.teamteorganiza.estoque.ui;

/** Conversões simples de texto dos formulários do estoque. */
final class CampoUtil {

    private CampoUtil() {}

    /** Lê um número (quantidade, custo, mínimo) aceitando vírgula ou ponto. */
    static double numero(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }
}
