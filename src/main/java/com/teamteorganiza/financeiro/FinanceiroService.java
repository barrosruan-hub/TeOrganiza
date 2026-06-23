package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceiroService {

    private final MensalidadeRepository mensalidadeRepo;
    private final List<MovimentacaoFinanceira> movimentacoes = new ArrayList<>();
    private final List<Vaquinha> vaquinhas = new ArrayList<>();
    private final CaixaEvento caixa = new CaixaEvento("Evento 1");

    public FinanceiroService(MensalidadeRepository mensalidadeRepo) {
        this.mensalidadeRepo = mensalidadeRepo;
    }

    // ===================== Mensalidades =====================

    public void emitirMensalidade(int pessoaId, String mes, double valor, LocalDate venc) {
        mensalidadeRepo.salvar(new Mensalidade(pessoaId, mes, valor, venc));
    }

    public void pagarMensalidade(int id) {
        mensalidadeRepo.buscarPorId(id).ifPresent(Mensalidade::pagar);
    }

    public List<Mensalidade> getMensalidades() { return mensalidadeRepo.listarTodos(); }

    // ===================== Entradas / Despesas =====================

    public MovimentacaoFinanceira registrarEntrada(int pessoaId, String descricao, double valor) {
        MovimentacaoFinanceira m = new MovimentacaoFinanceira(pessoaId, descricao, valor, TipoLancamento.RECEITA);
        movimentacoes.add(m);
        return m;
    }

    public MovimentacaoFinanceira registrarDespesa(int pessoaId, String descricao, double valor) {
        MovimentacaoFinanceira m = new MovimentacaoFinanceira(pessoaId, descricao, valor, TipoLancamento.DESPESA);
        movimentacoes.add(m);
        return m;
    }

    public List<MovimentacaoFinanceira> getEntradas() { return filtrar(TipoLancamento.RECEITA); }
    public List<MovimentacaoFinanceira> getDespesas() { return filtrar(TipoLancamento.DESPESA); }

    private List<MovimentacaoFinanceira> filtrar(TipoLancamento tipo) {
        List<MovimentacaoFinanceira> resultado = new ArrayList<>();
        for (MovimentacaoFinanceira m : movimentacoes) {
            if (m.getTipo() == tipo) resultado.add(m);
        }
        return resultado;
    }

    public void editarMovimentacao(int id, int pessoaId, String descricao, double valor) {
        for (MovimentacaoFinanceira m : movimentacoes) {
            if (m.getId() == id) {
                m.setPessoaId(pessoaId);
                m.setDescricao(descricao);
                m.setValor(valor);
                return;
            }
        }
    }

    public void removerMovimentacao(int id) {
        movimentacoes.removeIf(m -> m.getId() == id);
    }

    public double totalEntradas() { return somar(getEntradas()); }
    public double totalDespesas() { return somar(getDespesas()); }

    private double somar(List<MovimentacaoFinanceira> lista) {
        double total = 0;
        for (MovimentacaoFinanceira m : lista) total += m.getValor();
        return total;
    }

    // ===================== Caixa de evento =====================

    public CaixaEvento getCaixa() { return caixa; }
    public List<VendaCaixa> getVendasCaixa() { return caixa.getVendas(); }
    public double totalCaixa() { return caixa.total(); }
    public String getNomeEvento() { return caixa.getNomeEvento(); }
    public void setNomeEvento(String nome) { caixa.setNomeEvento(nome); }

    public VendaCaixa registrarVenda(int pessoaId, String descricao, double valor) {
        return caixa.registrarVenda(pessoaId, descricao, valor);
    }

    public void editarVenda(int id, int pessoaId, String descricao, double valor) {
        for (VendaCaixa v : caixa.getVendas()) {
            if (v.getId() == id) {
                v.setPessoaId(pessoaId);
                v.setDescricao(descricao);
                v.setValor(valor);
                return;
            }
        }
    }

    public void removerVenda(int id) {
        caixa.removerVenda(id);
    }

    /**
     * Fecha o caixa do evento atual: o total das vendas vira uma única
     * entrada no financeiro e o caixa é reiniciado para um novo evento.
     */
    public void fecharCaixa(String nomeNovoEvento) {
        double total = caixa.total();
        if (total > 0) {
            String desc = "Vendas do evento: " + caixa.getNomeEvento()
                    + " (" + caixa.quantidadeVendas() + " venda(s))";
            movimentacoes.add(new MovimentacaoFinanceira(0, desc, total, TipoLancamento.RECEITA));
        }
        caixa.novoEvento(nomeNovoEvento);
    }

    // ===================== Vaquinhas =====================

    public List<Vaquinha> getVaquinhas() { return new ArrayList<>(vaquinhas); }

    public Vaquinha criarVaquinha(String titulo, String objetivo, double meta) {
        Vaquinha v = new Vaquinha(titulo, objetivo, meta);
        vaquinhas.add(v);
        return v;
    }

    public ContribuicaoVaquinha contribuir(Vaquinha v, int pessoaId, double valor, String descricao) {
        return v.contribuir(pessoaId, valor, descricao);
    }

    public void editarContribuicao(Vaquinha v, int id, int pessoaId, String descricao, double valor) {
        for (ContribuicaoVaquinha c : v.getContribuicoes()) {
            if (c.getId() == id) {
                c.setPessoaId(pessoaId);
                c.setDescricao(descricao);
                c.setValor(valor);
                return;
            }
        }
    }

    public void removerContribuicao(Vaquinha v, int id) {
        v.removerContribuicao(id);
    }

    /** Top 3 maiores doadores somando as contribuições de todas as vaquinhas. */
    public List<Doador> top3Doadores() {
        Map<Integer, Double> totais = new HashMap<>();
        for (Vaquinha v : vaquinhas) {
            for (ContribuicaoVaquinha c : v.getContribuicoes()) {
                totais.merge(c.getPessoaId(), c.getValor(), Double::sum);
            }
        }
        return totais.entrySet().stream()
                .map(e -> new Doador(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(Doador::total).reversed())
                .limit(3)
                .toList();
    }

    // ===================== Extrato (consolidado) =====================

    public List<Lancamento> todosLancamentos() {
        List<Lancamento> todos = new ArrayList<>();
        todos.addAll(mensalidadeRepo.listarTodos());
        todos.addAll(movimentacoes);
        for (Vaquinha v : vaquinhas) todos.addAll(v.getContribuicoes());
        todos.addAll(caixa.getVendas());
        return todos;
    }
}
