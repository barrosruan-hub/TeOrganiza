package com.teamteorganiza.eventos.model;

import java.util.Arrays;
import java.util.List;

/**
 * Tipo de compromisso/evento do CTG. Cada tipo conhece as categorias possíveis
 * para ele, evitando "magic strings" espalhadas pela interface. As categorias de
 * INVERNADA vêm do enum {@link Invernada}.
 */
public enum TipoCompromisso {
    INVERNADA,
    CAMPEIRA,
    CTG,
    RESERVA_ESPACO,
    TRANSPORTE;

    /** Categorias disponíveis para este tipo (vazio quando não se aplica). */
    public List<String> categorias() {
        return switch (this) {
            case INVERNADA -> Arrays.stream(Invernada.values())
                    .map(Invernada::getDisplayName)
                    .toList();
            case CAMPEIRA -> List.of("Rodeio", "Tiro de Laço", "Treino", "Cavalgada");
            case CTG -> List.of("Baile", "Janta", "Reunião", "Festival", "Assembleia");
            default -> List.of();
        };
    }
}
