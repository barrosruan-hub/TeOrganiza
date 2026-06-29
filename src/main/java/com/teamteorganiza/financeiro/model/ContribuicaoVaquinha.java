package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class ContribuicaoVaquinha extends Lancamento {

    private String pessoaId;

    public ContribuicaoVaquinha(String pessoaId, double valor, String descricao) {
        super((descricao == null || descricao.isBlank()) ? "Doação" : descricao,
              valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
    }

    public String getPessoaId() { return pessoaId; }
    public void setPessoaId(String pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        return String.format("Contribuição #%s | pessoa %s | %s | R$ %.2f | %s",
                id, pessoaId, descricao, valor, data);
    }
}
