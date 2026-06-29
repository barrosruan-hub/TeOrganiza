package com.teamteorganiza.estoque.model;

/**
 * Item controlado no estoque do CTG (erva, bebida, material de limpeza, etc.).
 *
 * A quantidade é {@code double} para suportar unidades fracionadas (KG, L).
 * O {@code custoMedio} é o custo médio ponderado, recalculado a cada entrada
 * pelo {@code EstoqueService}.
 */
public class Produto {

    private static int idCounter = 0;

    private int id;
    private String nome;
    private String categoria;
    private UnidadeMedida unidade;
    private double quantidade;      // estoque atual
    private double estoqueMinimo;   // gatilho de reposição
    private double custoMedio;      // custo médio ponderado (R$ por unidade)

    public Produto(String nome, String categoria, UnidadeMedida unidade, double estoqueMinimo) {
        this.id = ++idCounter;
        this.nome = nome;
        this.categoria = categoria;
        this.unidade = unidade;
        this.estoqueMinimo = estoqueMinimo;
        this.quantidade = 0;
        this.custoMedio = 0;
    }

    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public UnidadeMedida getUnidade() { return unidade; }
    public void setUnidade(UnidadeMedida unidade) { this.unidade = unidade; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public double getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(double estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public double getCustoMedio() { return custoMedio; }
    public void setCustoMedio(double custoMedio) { this.custoMedio = custoMedio; }

    /** Está abaixo (ou no limite) do estoque mínimo e precisa de reposição. */
    public boolean abaixoDoMinimo() {
        return quantidade <= estoqueMinimo;
    }

    /** Valor total parado em estoque para este produto (quantidade x custo médio). */
    public double valorEmEstoque() {
        return quantidade * custoMedio;
    }

    @Override
    public String toString() {
        return String.format("#%d %s [%s] | qtd: %.2f %s | mín: %.2f | custo médio: R$ %.2f",
                id, nome, categoria, quantidade, unidade, estoqueMinimo, custoMedio);
    }
}
