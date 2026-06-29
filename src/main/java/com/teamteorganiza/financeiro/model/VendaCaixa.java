package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class VendaCaixa extends Lancamento {

    private String pessoaId;

    public VendaCaixa(String pessoaId, String descricao, double valor) {
        super(descricao, valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
    }

    public String getPessoaId() { return pessoaId; }
    public void setPessoaId(String pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        return String.format("Venda #%s | pessoa %s | %s | R$ %.2f | %s",
                id, pessoaId, descricao, valor, data);
    }
}
