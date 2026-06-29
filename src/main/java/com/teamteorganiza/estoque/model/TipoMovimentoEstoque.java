package com.teamteorganiza.estoque.model;

/**
 * Natureza de um movimento de estoque.
 * ENTRADA       - compra/reposição de mercadoria (aumenta o estoque).
 * BAIXA_CONSUMO - consumo interno (ex.: erva usada na cozinha do CTG).
 * BAIXA_VENDA   - saída por venda (ex.: bebida vendida no baile).
 */
public enum TipoMovimentoEstoque {
    ENTRADA, BAIXA_CONSUMO, BAIXA_VENDA;

    public boolean isBaixa() {
        return this == BAIXA_CONSUMO || this == BAIXA_VENDA;
    }
}
