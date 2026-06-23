package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.ContribuicaoVaquinha;
import com.teamteorganiza.financeiro.model.Lancamento;
import com.teamteorganiza.financeiro.model.Mensalidade;
import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;
import com.teamteorganiza.financeiro.model.TipoLancamento;
import com.teamteorganiza.financeiro.model.VendaCaixa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/** Aba somente leitura: consolida todos os lançamentos do financeiro. */
public class ExtratoTab extends JPanel {

    private final FinanceiroService service;
    private final Function<Integer, String> nomeResolver;
    private final DefaultTableModel tableModel;
    private final JLabel resumo = new JLabel(" ");

    public ExtratoTab(FinanceiroService service, Function<Integer, String> nomeResolver) {
        this.service = service;
        this.nomeResolver = nomeResolver;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel titulo = new JLabel("Extrato — todos os lançamentos (somente leitura)");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        add(titulo, BorderLayout.NORTH);

        String[] colunas = {"ID", "Tipo", "ID Pessoa", "Nome", "Valor", "Descrição", "Data"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabela = new JTable(tableModel);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        resumo.setFont(resumo.getFont().deriveFont(Font.BOLD, 13f));
        resumo.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
        add(resumo, BorderLayout.SOUTH);
    }

    public void recarregar() {
        tableModel.setRowCount(0);
        double receitas = 0, despesas = 0;
        List<Lancamento> lancamentos = service.todosLancamentos();
        for (Lancamento l : lancamentos) {
            int pessoaId = pessoaIdDe(l);
            tableModel.addRow(new Object[]{
                l.getId(),
                l.getTipo(),
                pessoaId == 0 ? "-" : pessoaId,
                nomeResolver.apply(pessoaId),
                String.format("R$ %.2f", l.getValor()),
                l.getDescricao(),
                l.getData()
            });
            if (l.getTipo() == TipoLancamento.RECEITA) receitas += l.getValor();
            else despesas += l.getValor();
        }
        resumo.setText(String.format(
                "Entradas: R$ %.2f      |      Despesas: R$ %.2f      |      Saldo: R$ %.2f",
                receitas, despesas, receitas - despesas));
    }

    private int pessoaIdDe(Lancamento l) {
        if (l instanceof MovimentacaoFinanceira m) return m.getPessoaId();
        if (l instanceof ContribuicaoVaquinha c) return c.getPessoaId();
        if (l instanceof VendaCaixa v) return v.getPessoaId();
        if (l instanceof Mensalidade me) return me.getPessoaId();
        return 0;
    }
}
