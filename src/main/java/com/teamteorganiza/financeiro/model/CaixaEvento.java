package com.teamteorganiza.financeiro.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Caixa de um evento: acumula as vendas (entradas) do evento corrente.
 * Ao fechar, o total deve ser registrado como uma única entrada no
 * financeiro e o caixa é reiniciado para um novo evento.
 */
public class CaixaEvento {

    private String nomeEvento;
    private final List<VendaCaixa> vendas = new ArrayList<>();

    public CaixaEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getNomeEvento() { return nomeEvento; }
    public void setNomeEvento(String nomeEvento) { this.nomeEvento = nomeEvento; }

    public List<VendaCaixa> getVendas() { return vendas; }

    public VendaCaixa registrarVenda(int pessoaId, String descricao, double valor) {
        VendaCaixa venda = new VendaCaixa(pessoaId, descricao, valor);
        vendas.add(venda);
        return venda;
    }

    public void removerVenda(int id) {
        vendas.removeIf(v -> v.getId() == id);
    }

    public double total() {
        double total = 0;
        for (VendaCaixa v : vendas) total += v.getValor();
        return total;
    }

    public int quantidadeVendas() {
        return vendas.size();
    }

    /** Reinicia o caixa para um novo evento, descartando as vendas atuais. */
    public void novoEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
        vendas.clear();
    }
}
