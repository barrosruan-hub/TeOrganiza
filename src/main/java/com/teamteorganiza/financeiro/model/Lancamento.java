package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;
import java.util.UUID;

public abstract class Lancamento {

    protected final String id;
    protected String descricao;
    protected double valor;
    protected LocalDate data;
    protected TipoLancamento tipo;

    protected Lancamento(String descricao, double valor, LocalDate data, TipoLancamento tipo) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
    }

    protected Lancamento(String id, String descricao, double valor, LocalDate data, TipoLancamento tipo) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
    }

    public String getId() { return id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }

    public String mesDoLancamento() {
        return data.toString().substring(0, 7);
    }

    public abstract String detalhar();

    @Override
    public String toString() {
        return String.format("#%s [%s] %s - R$ %.2f (%s)", id, tipo, descricao, valor, data);
    }
}
