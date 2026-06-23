package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

/**
 * Uma venda individual registrada no caixa de um evento.
 * Fica no {@link CaixaEvento} até o caixa ser fechado, quando o total
 * vira uma única entrada no financeiro.
 */
public class VendaCaixa extends Lancamento {

    private int pessoaId;

    public VendaCaixa(int pessoaId, String descricao, double valor) {
        super(descricao, valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
    }

    public int getPessoaId() { return pessoaId; }
    public void setPessoaId(int pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        return String.format("Venda #%d | pessoa %d | %s | R$ %.2f | %s",
                id, pessoaId, descricao, valor, data);
    }
}
