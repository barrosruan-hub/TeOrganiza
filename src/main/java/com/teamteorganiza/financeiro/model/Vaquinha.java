package com.teamteorganiza.financeiro.model;

import java.util.ArrayList;
import java.util.List;

public class Vaquinha {

    private String titulo;
    private String objetivo;
    private double meta;
    private ArrayList<ContribuicaoVaquinha> contribuicoes;

    public Vaquinha(String titulo, String objetivo, double meta) {
        this.titulo = titulo;
        this.objetivo = objetivo;
        this.meta = meta;
        this.contribuicoes = new ArrayList<>();
    }

    public String getTitulo() { return titulo; }
    public String getObjetivo() { return objetivo; }
    public double getMeta() { return meta; }
    public List<ContribuicaoVaquinha> getContribuicoes() { return contribuicoes; }

    public ContribuicaoVaquinha contribuir(String pessoaId, double valor, String descricao) {
        ContribuicaoVaquinha c = new ContribuicaoVaquinha(pessoaId, valor, descricao);
        contribuicoes.add(c);
        return c;
    }

    public void removerContribuicao(String id) {
        contribuicoes.removeIf(c -> c.getId().equals(id));
    }

    public double totalArrecadado() {
        double total = 0;
        for (ContribuicaoVaquinha c : contribuicoes) total += c.getValor();
        return total;
    }

    public double quantoFalta() { return Math.max(meta - totalArrecadado(), 0); }

    public boolean metaAtingida() { return totalArrecadado() >= meta; }
}
