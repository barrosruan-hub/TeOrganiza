package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class Mensalidade extends Lancamento {

    private final String pessoaId;
    private final String mesReferencia;
    private final LocalDate vencimento;
    private StatusMensalidade status;

    public Mensalidade(String pessoaId, String mesReferencia, double valor, LocalDate vencimento) {
        super("Mensalidade " + mesReferencia, valor, vencimento, TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.mesReferencia = mesReferencia;
        this.vencimento = vencimento;
        this.status = StatusMensalidade.EM_ABERTO;
    }

    public Mensalidade(String id, String pessoaId, String mesReferencia, double valor,
                       LocalDate vencimento, StatusMensalidade status) {
        super(id, "Mensalidade " + mesReferencia, valor, vencimento, TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.mesReferencia = mesReferencia;
        this.vencimento = vencimento;
        this.status = status;
    }

    public String getPessoaId() { return pessoaId; }
    public String getMesReferencia() { return mesReferencia; }
    public LocalDate getVencimento() { return vencimento; }
    public StatusMensalidade getStatus() { return status; }
    public void setStatus(StatusMensalidade status) { this.status = status; }

    public void pagar() { this.status = StatusMensalidade.PAGA; }

    public boolean estaAtrasada() {
        if (status == StatusMensalidade.PAGA) return false;
        boolean venceu = LocalDate.now().isAfter(vencimento);
        if (venceu) status = StatusMensalidade.ATRASADA;
        return venceu;
    }

    @Override
    public String mesDoLancamento() { return mesReferencia; }

    @Override
    public String detalhar() {
        return String.format("Mensalidade #%s | pessoa %s | ref %s | venc %s | R$ %.2f | %s",
                id, pessoaId, mesReferencia, vencimento, valor, status);
    }
}
