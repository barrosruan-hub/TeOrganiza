package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

/**
 * Lançamento avulso de Entrada (RECEITA) ou Despesa (DESPESA), sempre
 * vinculado à pessoa que movimentou o dinheiro.
 */
public class MovimentacaoFinanceira extends Lancamento {

    private int pessoaId;

    public MovimentacaoFinanceira(int pessoaId, String descricao, double valor, TipoLancamento tipo) {
        super(descricao, valor, LocalDate.now(), tipo);
        this.pessoaId = pessoaId;
    }

    public int getPessoaId() { return pessoaId; }
    public void setPessoaId(int pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        String sinal = (tipo == TipoLancamento.RECEITA) ? "+" : "-";
        return String.format("#%d | pessoa %d | %s | %sR$ %.2f | %s",
                id, pessoaId, descricao, sinal, valor, data);
    }
}
