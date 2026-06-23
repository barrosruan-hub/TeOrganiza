package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;
import com.teamteorganiza.financeiro.model.TipoLancamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/** Aba CRUD reutilizável para Entradas (RECEITA) e Despesas (DESPESA). */
public class MovimentacaoTab extends JPanel {

    private final FinanceiroService service;
    private final Function<Integer, String> nomeResolver;
    private final Runnable onChange;
    private final TipoLancamento tipo;

    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final JTextField tfId = new JTextField(8);
    private final JTextArea taDescricao = new JTextArea(3, 24);
    private final JTextField tfValor = new JTextField(10);

    private List<MovimentacaoFinanceira> linhas = List.of();

    public MovimentacaoTab(FinanceiroService service, Function<Integer, String> nomeResolver,
                           Runnable onChange, TipoLancamento tipo) {
        this.service = service;
        this.nomeResolver = nomeResolver;
        this.onChange = onChange;
        this.tipo = tipo;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] colunas = {"ID Pessoa", "Nome", "Valor", "Descrição"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private boolean isEntrada() { return tipo == TipoLancamento.RECEITA; }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder(isEntrada() ? "Entrada" : "Despesa"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        taDescricao.setLineWrap(true);
        taDescricao.setWrapStyleWord(true);

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("ID (pessoa):"), c);
        c.gridx = 1; c.gridy = 0; painel.add(tfId, c);

        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.NORTHWEST;
        painel.add(new JLabel("Descrição:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(new JScrollPane(taDescricao), c);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Valor:"), c);
        c.gridx = 1; c.gridy = 2; painel.add(tfValor, c);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnCriar = new JButton("Criar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDeletar = new JButton("Deletar");
        JButton btnLimpar = new JButton("Limpar");
        botoes.add(btnCriar); botoes.add(btnEditar); botoes.add(btnDeletar); botoes.add(btnLimpar);
        c.gridx = 1; c.gridy = 3; painel.add(botoes, c);

        btnCriar.addActionListener(e -> criar());
        btnEditar.addActionListener(e -> editar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limpar());

        return painel;
    }

    private void criar() {
        Integer id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        if (isEntrada()) service.registrarEntrada(id, taDescricao.getText().trim(), valor);
        else service.registrarDespesa(id, taDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um item para editar."); return; }
        Integer id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        service.editarMovimentacao(linhas.get(row).getId(), id, taDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um item para deletar."); return; }
        service.removerMovimentacao(linhas.get(row).getId());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        MovimentacaoFinanceira m = linhas.get(row);
        tfId.setText(String.valueOf(m.getPessoaId()));
        taDescricao.setText(m.getDescricao());
        tfValor.setText(String.format("%.2f", m.getValor()));
    }

    private void limpar() {
        tfId.setText("");
        taDescricao.setText("");
        tfValor.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        linhas = isEntrada() ? service.getEntradas() : service.getDespesas();
        tableModel.setRowCount(0);
        for (MovimentacaoFinanceira m : linhas) {
            tableModel.addRow(new Object[]{
                m.getPessoaId(),
                nomeResolver.apply(m.getPessoaId()),
                String.format("R$ %.2f", m.getValor()),
                m.getDescricao()
            });
        }
    }

    private Integer lerId() {
        try {
            return CampoUtil.id(tfId.getText());
        } catch (NumberFormatException ex) {
            aviso("ID da pessoa inválido.");
            return null;
        }
    }

    private Double lerValor() {
        try {
            return CampoUtil.valor(tfValor.getText());
        } catch (NumberFormatException ex) {
            aviso("Valor inválido.");
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
