package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class ContribuicaoVaquinha extends Lancamento {

    private int pessoaId;

    public ContribuicaoVaquinha(int pessoaId, double valor, String descricao) {
        super((descricao == null || descricao.isBlank()) ? "Doação pessoa " + pessoaId : descricao,
              valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
    }

    public int getPessoaId() { return pessoaId; }
    public void setPessoaId(int pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        return String.format("Contribuição #%d | pessoa %d | %s | R$ %.2f | %s",
                id, pessoaId, descricao, valor, data);
    }
}
