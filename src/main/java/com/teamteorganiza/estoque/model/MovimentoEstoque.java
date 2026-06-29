package com.teamteorganiza.estoque.model;

import java.time.LocalDate;

/**
 * Registro histórico de uma entrada ou baixa de estoque.
 * Funciona como um "extrato" do estoque, no mesmo espírito das
 * MovimentacaoFinanceira do módulo Financeiro.
 *
 * {@code custoUnitario} só é relevante em movimentos de ENTRADA (preço pago na
 * compra); nas baixas fica 0.
 */
public class MovimentoEstoque {

    private static int idCounter = 0;

    private int id;
    private int produtoId;
    private TipoMovimentoEstoque tipo;
    private double quantidade;
    private double custoUnitario;   // usado apenas em ENTRADA
    private LocalDate data;
    private String observacao;

    public MovimentoEstoque(int produtoId, TipoMovimentoEstoque tipo, double quantidade,
                            double custoUnitario, String observacao) {
        this.id = ++idCounter;
        this.produtoId = produtoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.data = LocalDate.now();
        this.observacao = observacao;
    }

    public int getId() { return id; }
    public int getProdutoId() { return produtoId; }
    public TipoMovimentoEstoque getTipo() { return tipo; }
    public double getQuantidade() { return quantidade; }
    public double getCustoUnitario() { return custoUnitario; }
    public LocalDate getData() { return data; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    @Override
    public String toString() {
        String sinal = tipo.isBaixa() ? "-" : "+";
        return String.format("#%d | produto %d | %s | %s%.2f | %s",
                id, produtoId, tipo, sinal, quantidade, data);
    }
}
