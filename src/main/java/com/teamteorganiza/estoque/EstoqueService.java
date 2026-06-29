package com.teamteorganiza.estoque;

import com.teamteorganiza.estoque.model.MovimentoEstoque;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.TipoMovimentoEstoque;
import com.teamteorganiza.estoque.model.UnidadeMedida;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do módulo de Estoque.
 *
 * Segue o mesmo padrão dos outros módulos: recebe o repositório no construtor,
 * guarda o histórico de movimentos em memória e valida com
 * {@link IllegalArgumentException}.
 */
public class EstoqueService {

    private final EstoqueRepository repositorio;
    private final List<MovimentoEstoque> movimentos = new ArrayList<>();

    public EstoqueService(EstoqueRepository repositorio) {
        this.repositorio = repositorio;
    }

    // ===================== Cadastro de produto =====================

    /**
     * (1) Adicionar item — cadastra um novo produto no catálogo do estoque.
     * Nasce com quantidade 0 e custo médio 0; a quantidade entra via
     * {@link #registrarEntrada}.
     */
    public Produto cadastrarProduto(String nome, String categoria, UnidadeMedida unidade,
                                    double estoqueMinimo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (estoqueMinimo < 0) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        }
        repositorio.buscarPorNome(nome).ifPresent(p -> {
            throw new IllegalArgumentException("Já existe um produto com o nome: " + nome);
        });
        Produto produto = new Produto(nome, categoria, unidade, estoqueMinimo);
        repositorio.salvar(produto);
        return produto;
    }

    public List<Produto> listar() {
        return repositorio.listarTodos();
    }

    /** Edita os dados cadastrais do produto (não mexe em quantidade nem custo médio). */
    public void editarProduto(int id, String nome, String categoria, UnidadeMedida unidade,
                              double estoqueMinimo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        }
        if (estoqueMinimo < 0) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        }
        repositorio.buscarPorNome(nome)
                .filter(outro -> outro.getId() != id)
                .ifPresent(outro -> {
                    throw new IllegalArgumentException("Já existe outro produto com o nome: " + nome);
                });
        Produto produto = buscarProduto(id);
        produto.setNome(nome);
        produto.setCategoria(categoria);
        produto.setUnidade(unidade);
        produto.setEstoqueMinimo(estoqueMinimo);
    }

    public void removerProduto(int id) {
        repositorio.remover(id);
    }

    public Produto buscarProduto(int produtoId) {
        return repositorio.buscarPorId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: #" + produtoId));
    }

    // ===================== Entrada de mercadoria =====================

    /**
     * (4) Atualizar custo médio — registra uma ENTRADA de mercadoria, soma a
     * quantidade ao estoque e recalcula o custo médio ponderado do produto.
     *
     * Fórmula do custo médio ponderado:
     * <pre>
     *   novoCusto = (qtdAtual * custoMedioAtual + qtdEntrada * custoUnitario)
     *               / (qtdAtual + qtdEntrada)
     * </pre>
     *
     * @param custoUnitario preço pago por unidade nesta compra (R$).
     */
    public MovimentoEstoque registrarEntrada(int produtoId, double quantidade, double custoUnitario) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade da entrada deve ser maior que zero.");
        }
        if (custoUnitario < 0) {
            throw new IllegalArgumentException("Custo unitário não pode ser negativo.");
        }
        Produto produto = buscarProduto(produtoId);

        // Recalcula o custo médio ANTES de alterar a quantidade (usa o saldo atual).
        atualizarCustoMedio(produto, quantidade, custoUnitario);
        produto.setQuantidade(produto.getQuantidade() + quantidade);

        MovimentoEstoque mov = new MovimentoEstoque(produtoId, TipoMovimentoEstoque.ENTRADA,
                quantidade, custoUnitario, "Entrada de mercadoria");
        movimentos.add(mov);
        return mov;
    }

    /**
     * Recalcula o custo médio ponderado do produto considerando uma nova entrada.
     * Mantido privado porque o custo médio nunca deve ser editado "na mão":
     * ele é sempre derivado das entradas.
     */
    private void atualizarCustoMedio(Produto produto, double qtdEntrada, double custoUnitario) {
        double qtdAtual = produto.getQuantidade();
        double valorEstoqueAtual = qtdAtual * produto.getCustoMedio();
        double valorEntrada = qtdEntrada * custoUnitario;
        double novaQtd = qtdAtual + qtdEntrada;

        double novoCustoMedio = (novaQtd > 0) ? (valorEstoqueAtual + valorEntrada) / novaQtd : 0;
        produto.setCustoMedio(novoCustoMedio);
    }

    // ===================== Baixa (consumo / venda) =====================

    /**
     * (2) Dar baixa por consumo/venda — reduz a quantidade do produto.
     * Não recalcula o custo médio (no método do custo médio ponderado, a saída
     * sai pelo custo médio vigente e não altera o custo unitário do que sobra).
     *
     * @param tipo deve ser BAIXA_CONSUMO ou BAIXA_VENDA.
     */
    public MovimentoEstoque darBaixa(int produtoId, double quantidade,
                                     TipoMovimentoEstoque tipo, String observacao) {
        if (tipo == null || !tipo.isBaixa()) {
            throw new IllegalArgumentException("Tipo de baixa inválido. Use BAIXA_CONSUMO ou BAIXA_VENDA.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade da baixa deve ser maior que zero.");
        }
        Produto produto = buscarProduto(produtoId);
        if (produto.getQuantidade() < quantidade) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente de '%s': disponível %.2f, solicitado %.2f.",
                    produto.getNome(), produto.getQuantidade(), quantidade));
        }
        produto.setQuantidade(produto.getQuantidade() - quantidade);

        MovimentoEstoque mov = new MovimentoEstoque(produtoId, tipo, quantidade, 0, observacao);
        movimentos.add(mov);
        return mov;
    }

    // ===================== Alertas de reposição =====================

    /**
     * (3) Listar produtos abaixo do estoque mínimo — para reposição.
     */
    public List<Produto> listarAbaixoDoEstoqueMinimo() {
        return repositorio.listarTodos().stream()
                .filter(Produto::abaixoDoMinimo)
                .toList();
    }

    // ===================== Histórico / relatórios =====================

    public List<MovimentoEstoque> getMovimentos() {
        return new ArrayList<>(movimentos);
    }

    public List<MovimentoEstoque> getMovimentosPorProduto(int produtoId) {
        return movimentos.stream()
                .filter(m -> m.getProdutoId() == produtoId)
                .toList();
    }

    /** Valor total imobilizado em estoque (soma de quantidade x custo médio). */
    public double valorTotalEmEstoque() {
        double total = 0;
        for (Produto p : repositorio.listarTodos()) {
            total += p.valorEmEstoque();
        }
        return total;
    }
}
