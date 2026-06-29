package com.teamteorganiza.eventos.model;

/**
 * Categorias (faixas) de uma invernada de CTG. O nome de exibição é usado na
 * interface; o {@code toString} já devolve esse nome para aparecer bonito nos
 * combos e tabelas.
 */
public enum Invernada {
    PRE_MIRIM("Pré-Mirim"),
    MIRIM("Mirim"),
    JUVENIL("Juvenil"),
    ADULTA("Adulta"),
    VETERANA("Veterana"),
    XIRU("Xirú");

    private final String displayName;

    Invernada(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
