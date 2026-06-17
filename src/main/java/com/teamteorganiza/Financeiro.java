package com.teamteorganiza;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

enum TipoLancamento { RECEITA, DESPESA }

enum FormaPagamento { PIX, DINHEIRO, CARTAO, FICHA }

enum TipoCaixa { CTG, INVERNADA, CAMPEIRA }

enum StatusMensalidade { EM_ABERTO, PAGA, ATRASADA }

abstract class Lancamento {

    private static int contadorId = 1;

    protected int id;
    protected String descricao;
    protected double valor;
    protected LocalDate data;
    protected TipoLancamento tipo;

    protected Lancamento(String descricao, double valor, LocalDate data, TipoLancamento tipo) {
        this.id = contadorId++;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
    }

    public int getId() { return id; }

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
        return String.format("#%d [%s] %s - R$ %.2f (%s)",
                id, tipo, descricao, valor, data);
    }
}

class Mensalidade extends Lancamento {

    private int pessoaId;
    private String mesReferencia;
    private LocalDate vencimento;
    private StatusMensalidade status;

    public Mensalidade(int pessoaId, String mesReferencia, double valor, LocalDate vencimento) {
        super("Mensalidade " + mesReferencia + " (pessoa " + pessoaId + ")",
              valor, vencimento, TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.mesReferencia = mesReferencia;
        this.vencimento = vencimento;
        this.status = StatusMensalidade.EM_ABERTO;
    }

    public int getPessoaId() { return pessoaId; }
    public String getMesReferencia() { return mesReferencia; }
    public LocalDate getVencimento() { return vencimento; }
    public StatusMensalidade getStatus() { return status; }

    public void pagar() {
        this.status = StatusMensalidade.PAGA;
    }

    public boolean estaAtrasada() {
        if (status == StatusMensalidade.PAGA) {
            return false;
        }
        boolean venceu = LocalDate.now().isAfter(vencimento);
        if (venceu) {
            status = StatusMensalidade.ATRASADA;
        }
        return venceu;
    }

    @Override
    public String mesDoLancamento() {
        return mesReferencia;
    }

    @Override
    public String detalhar() {
        return String.format("Mensalidade #%d | pessoa %d | ref %s | venc %s | R$ %.2f | %s",
                id, pessoaId, mesReferencia, vencimento, valor, status);
    }
}


class LancamentoCaixa extends Lancamento {

    private TipoCaixa caixa;
    private String responsavel;

    public LancamentoCaixa(String descricao, double valor, LocalDate data,
                           TipoLancamento tipo, TipoCaixa caixa, String responsavel) {
        super(descricao, valor, data, tipo);
        this.caixa = caixa;
        this.responsavel = responsavel;
    }

    public TipoCaixa getCaixa() { return caixa; }
    public String getResponsavel() { return responsavel; }

    @Override
    public String detalhar() {
        String sinal = (tipo == TipoLancamento.RECEITA) ? "+" : "-";
        return String.format("Caixa %s #%d | %s | %sR$ %.2f | %s | resp: %s",
                caixa, id, descricao, sinal, valor, data, responsavel);
    }
}

class Caixa {

    private TipoCaixa tipo;
    private ArrayList<LancamentoCaixa> movimentos;

    public Caixa(TipoCaixa tipo) {
        this.tipo = tipo;
        this.movimentos = new ArrayList<>();
    }

    public TipoCaixa getTipo() { return tipo; }
    public List<LancamentoCaixa> getMovimentos() { return movimentos; }

    public void registrarEntrada(String descricao, double valor, String responsavel) {
        movimentos.add(new LancamentoCaixa(descricao, valor, LocalDate.now(),
                TipoLancamento.RECEITA, tipo, responsavel));
    }

    public void registrarSaida(String descricao, double valor, String responsavel) {
        movimentos.add(new LancamentoCaixa(descricao, valor, LocalDate.now(),
                TipoLancamento.DESPESA, tipo, responsavel));
    }

    public double saldoAtual() {
        double saldo = 0;
        for (LancamentoCaixa m : movimentos) {
            if (m.getTipo() == TipoLancamento.RECEITA) {
                saldo += m.getValor();
            } else {
                saldo -= m.getValor();
            }
        }
        return saldo;
    }

    public void extrato() {
        System.out.println("===== Extrato do caixa " + tipo + " =====");
        if (movimentos.isEmpty()) {
            System.out.println("(sem movimentações)");
        }
        for (LancamentoCaixa m : movimentos) {
            System.out.println(m.detalhar());
        }
        System.out.printf("Saldo atual: R$ %.2f%n", saldoAtual());
    }
}

class ContribuicaoVaquinha extends Lancamento {

    private int pessoaId;
    private boolean anonima;

    public ContribuicaoVaquinha(int pessoaId, double valor, boolean anonima) {
        super(anonima ? "Doação anônima" : "Doação pessoa " + pessoaId,
              valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.anonima = anonima;
    }

    public int getPessoaId() { return pessoaId; }
    public boolean isAnonima() { return anonima; }

    @Override
    public String detalhar() {
        String quem = anonima ? "anônimo" : ("pessoa " + pessoaId);
        return String.format("Contribuição #%d | %s | R$ %.2f | %s",
                id, quem, valor, data);
    }
}

class Vaquinha {

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

    public void contribuir(int pessoaId, double valor, boolean anonima) {
        contribuicoes.add(new ContribuicaoVaquinha(pessoaId, valor, anonima));
    }

    public double totalArrecadado() {
        double total = 0;
        for (ContribuicaoVaquinha c : contribuicoes) {
            total += c.getValor();
        }
        return total;
    }

    public double quantoFalta() {
        double falta = meta - totalArrecadado();
        return Math.max(falta, 0);
    }

    public boolean metaAtingida() {
        return totalArrecadado() >= meta;
    }

    public void listarContribuicoes() {
        System.out.println("===== Vaquinha: " + titulo + " =====");
        System.out.println("Objetivo: " + objetivo);
        if (contribuicoes.isEmpty()) {
            System.out.println("(nenhuma contribuição ainda)");
        }
        for (ContribuicaoVaquinha c : contribuicoes) {
            System.out.println(c.detalhar());
        }
        System.out.printf("Arrecadado: R$ %.2f de R$ %.2f | Falta: R$ %.2f | Meta atingida: %s%n",
                totalArrecadado(), meta, quantoFalta(), metaAtingida() ? "SIM" : "não");
    }
}

class VendaEvento extends Lancamento {

    private int quantidadeFichas;
    private FormaPagamento forma;

    public VendaEvento(int quantidadeFichas, double valor, FormaPagamento forma) {
        super(quantidadeFichas + " ficha(s) - " + forma,
              valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.quantidadeFichas = quantidadeFichas;
        this.forma = forma;
    }

    public int getQuantidadeFichas() { return quantidadeFichas; }
    public FormaPagamento getForma() { return forma; }

    @Override
    public String detalhar() {
        return String.format("Venda #%d | %d ficha(s) | R$ %.2f | %s | %s",
                id, quantidadeFichas, valor, forma, data);
    }
}

class Evento {

    private String nome;
    private LocalDate data;
    private double precoFicha;
    private ArrayList<VendaEvento> vendas;

    public Evento(String nome, LocalDate data, double precoFicha) {
        this.nome = nome;
        this.data = data;
        this.precoFicha = precoFicha;
        this.vendas = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public LocalDate getData() { return data; }
    public double getPrecoFicha() { return precoFicha; }
    public List<VendaEvento> getVendas() { return vendas; }

    public void venderFichas(int quantidade, FormaPagamento forma) {
        double total = quantidade * precoFicha;
        vendas.add(new VendaEvento(quantidade, total, forma));
    }

    public double totalArrecadado() {
        double total = 0;
        for (VendaEvento v : vendas) {
            total += v.getValor();
        }
        return total;
    }

    public int totalFichasVendidas() {
        int total = 0;
        for (VendaEvento v : vendas) {
            total += v.getQuantidadeFichas();
        }
        return total;
    }

    public void relatorioVendas() {
        System.out.println("===== Evento: " + nome + " (" + data + ") =====");
        System.out.printf("Preço da ficha: R$ %.2f%n", precoFicha);
        if (vendas.isEmpty()) {
            System.out.println("(nenhuma venda registrada)");
        }
        for (VendaEvento v : vendas) {
            System.out.println(v.detalhar());
        }
        System.out.printf("Total de fichas: %d | Total arrecadado: R$ %.2f%n",
                totalFichasVendidas(), totalArrecadado());
    }
}

class Dashboard {

    private Financeiro financeiro;

    public Dashboard(Financeiro financeiro) {
        this.financeiro = financeiro;
    }

    public void resumoMensal(String mesReferencia) {
        double receitas = 0;
        double despesas = 0;

        for (Lancamento l : financeiro.todosLancamentos()) {
            if (!l.mesDoLancamento().equals(mesReferencia)) {
                continue;
            }
            if (l.getTipo() == TipoLancamento.RECEITA) {
                receitas += l.getValor();
            } else {
                despesas += l.getValor();
            }
        }

        System.out.println("===== Resumo mensal: " + mesReferencia + " =====");
        System.out.printf("Receitas: R$ %.2f%n", receitas);
        System.out.printf("Despesas: R$ %.2f%n", despesas);
        System.out.printf("Saldo:    R$ %.2f%n", receitas - despesas);
    }

    public void gastosPorCategoria() {
        Map<String, Double> receitasPorCat = new LinkedHashMap<>();
        Map<String, Double> despesasPorCat = new LinkedHashMap<>();

        for (Lancamento l : financeiro.todosLancamentos()) {
            String categoria = categoriaDe(l);
            Map<String, Double> alvo =
                    (l.getTipo() == TipoLancamento.RECEITA) ? receitasPorCat : despesasPorCat;
            alvo.merge(categoria, l.getValor(), Double::sum);
        }

        System.out.println("===== Movimentação por categoria =====");
        System.out.println("-- Receitas --");
        imprimirMapa(receitasPorCat);
        System.out.println("-- Despesas --");
        imprimirMapa(despesasPorCat);
    }

    public void prestacaoDeContas() {
        System.out.println("======================================");
        System.out.println("        PRESTAÇÃO DE CONTAS");
        System.out.println("======================================");

        double receitas = 0;
        double despesas = 0;

        System.out.println("-- Lançamentos --");
        for (Lancamento l : financeiro.todosLancamentos()) {
            System.out.println(l.detalhar());
            if (l.getTipo() == TipoLancamento.RECEITA) {
                receitas += l.getValor();
            } else {
                despesas += l.getValor();
            }
        }

        System.out.println("--------------------------------------");
        System.out.printf("Total de entradas: R$ %.2f%n", receitas);
        System.out.printf("Total de saídas:   R$ %.2f%n", despesas);
        System.out.printf("RESULTADO:         R$ %.2f%n", receitas - despesas);
        System.out.println("======================================");
    }


    private String categoriaDe(Lancamento l) {
        if (l instanceof Mensalidade) {
            return "Mensalidades";
        } else if (l instanceof LancamentoCaixa lc) {
            return "Caixa " + lc.getCaixa();
        } else if (l instanceof ContribuicaoVaquinha) {
            return "Vaquinha";
        } else if (l instanceof VendaEvento) {
            return "Baile";
        }
        return "Outros";
    }

    private void imprimirMapa(Map<String, Double> mapa) {
        if (mapa.isEmpty()) {
            System.out.println("(nada)");
            return;
        }
        for (Map.Entry<String, Double> e : mapa.entrySet()) {
            System.out.printf("  %-18s R$ %.2f%n", e.getKey(), e.getValue());
        }
    }
}

public class Financeiro {

    private ArrayList<Mensalidade> mensalidades;
    private ArrayList<Caixa> caixas;
    private ArrayList<Vaquinha> vaquinhas;
    private ArrayList<Evento> bailes;
    private Dashboard dashboard;

    public Financeiro() {
        this.mensalidades = new ArrayList<>();
        this.caixas = new ArrayList<>();
        this.vaquinhas = new ArrayList<>();
        this.bailes = new ArrayList<>();

        for (TipoCaixa t : TipoCaixa.values()) {
            caixas.add(new Caixa(t));
        }

        this.dashboard = new Dashboard(this);
    }


    public void emitirMensalidade(int pessoaId, String mes, double valor, LocalDate venc) {
        mensalidades.add(new Mensalidade(pessoaId, mes, valor, venc));
    }

    public void pagarMensalidade(int id) {
        for (Mensalidade m : mensalidades) {
            if (m.getId() == id) {
                m.pagar();
                return;
            }
        }
        System.out.println("Mensalidade #" + id + " não encontrada.");
    }


    public Caixa getCaixa(TipoCaixa tipo) {
        for (Caixa c : caixas) {
            if (c.getTipo() == tipo) {
                return c;
            }
        }
        return null;
    }


    public Vaquinha criarVaquinha(String titulo, String objetivo, double meta) {
        Vaquinha v = new Vaquinha(titulo, objetivo, meta);
        vaquinhas.add(v);
        return v;
    }


    public Evento criarBaile(String nome, LocalDate data, double precoFicha) {
        Evento b = new Evento(nome, data, precoFicha);
        bailes.add(b);
        return b;
    }


    public void verDashboard(String mes) {
        dashboard.resumoMensal(mes);
        System.out.println();
        dashboard.gastosPorCategoria();
        System.out.println();
        dashboard.prestacaoDeContas();
    }


    public List<Mensalidade> getMensalidades() { return mensalidades; }
    public List<Caixa> getCaixas() { return caixas; }
    public List<Vaquinha> getVaquinhas() { return vaquinhas; }
    public List<Evento> getBailes() { return bailes; }
    public Dashboard getDashboard() { return dashboard; }


    List<Lancamento> todosLancamentos() {
        List<Lancamento> todos = new ArrayList<>();
        todos.addAll(mensalidades);
        for (Caixa c : caixas) {
            todos.addAll(c.getMovimentos());
        }
        for (Vaquinha v : vaquinhas) {
            todos.addAll(v.getContribuicoes());
        }
        for (Evento b : bailes) {
            todos.addAll(b.getVendas());
        }
        return todos;
    }
}
