package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.Doador;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.ContribuicaoVaquinha;
import com.teamteorganiza.financeiro.model.Vaquinha;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/** Aba Vaquinhas: contribuições por vaquinha + ranking dos 3 maiores doadores. */
public class VaquinhaTab extends JPanel {

    private final FinanceiroService service;
    private final Function<Integer, String> nomeResolver;
    private final Runnable onChange;

    private final JComboBox<VaquinhaItem> combo = new JComboBox<>();
    private final JLabel lblInfo = new JLabel(" ");
    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final DefaultTableModel topModel;
    private final JTextField tfId = new JTextField(8);
    private final JTextArea taDescricao = new JTextArea(3, 20);
    private final JTextField tfValor = new JTextField(10);

    private List<ContribuicaoVaquinha> linhas = List.of();
    private boolean atualizandoCombo = false;

    public VaquinhaTab(FinanceiroService service, Function<Integer, String> nomeResolver, Runnable onChange) {
        this.service = service;
        this.nomeResolver = nomeResolver;
        this.onChange = onChange;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(montarTopo(), BorderLayout.NORTH);

        String[] colunas = {"ID Pessoa", "Nome", "Valor", "Descrição"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });

        topModel = new DefaultTableModel(new String[]{"#", "ID", "Nome", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable topTabela = new JTable(topModel);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Top 3 doadores (todas as vaquinhas)"));
        topPanel.add(new JScrollPane(topTabela), BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(280, 0));

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        centro.add(topPanel, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topo.add(new JLabel("Vaquinha:"));
        topo.add(combo);
        JButton btnNova = new JButton("Nova vaquinha");
        topo.add(btnNova);
        topo.add(Box.createHorizontalStrut(16));
        topo.add(lblInfo);

        combo.addActionListener(e -> {
            if (atualizandoCombo) return;
            recarregarTabela();
            atualizarInfo();
        });
        btnNova.addActionListener(e -> novaVaquinha());
        return topo;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Contribuição"));
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

    private void novaVaquinha() {
        JTextField tfTitulo = new JTextField(18);
        JTextField tfObjetivo = new JTextField(18);
        JTextField tfMeta = new JTextField(10);
        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Título:"));   form.add(tfTitulo);
        form.add(new JLabel("Objetivo:")); form.add(tfObjetivo);
        form.add(new JLabel("Meta (R$):")); form.add(tfMeta);

        int opcao = JOptionPane.showConfirmDialog(this, form, "Nova vaquinha",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        String titulo = tfTitulo.getText().trim();
        if (titulo.isEmpty()) { aviso("Informe o título da vaquinha."); return; }
        double meta;
        try {
            meta = CampoUtil.valor(tfMeta.getText());
        } catch (NumberFormatException ex) {
            aviso("Meta inválida.");
            return;
        }
        Vaquinha nova = service.criarVaquinha(titulo, tfObjetivo.getText().trim(), meta);
        onChange.run();
        selecionar(nova);
    }

    private void criar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) { aviso("Crie ou selecione uma vaquinha primeiro."); return; }
        Integer id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        service.contribuir(v, id, valor, taDescricao.getText().trim());
        limpar();
        onChange.run();
    }

    private void editar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) return;
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma contribuição para editar."); return; }
        Integer id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        service.editarContribuicao(v, linhas.get(row).getId(), id, taDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void deletar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) return;
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma contribuição para deletar."); return; }
        service.removerContribuicao(v, linhas.get(row).getId());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        ContribuicaoVaquinha c = linhas.get(row);
        tfId.setText(String.valueOf(c.getPessoaId()));
        taDescricao.setText(c.getDescricao());
        tfValor.setText(String.format("%.2f", c.getValor()));
    }

    private void limpar() {
        tfId.setText("");
        taDescricao.setText("");
        tfValor.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        atualizandoCombo = true;
        Vaquinha selecionada = vaquinhaSelecionada();
        combo.removeAllItems();
        List<Vaquinha> vaquinhas = service.getVaquinhas();
        for (Vaquinha v : vaquinhas) combo.addItem(new VaquinhaItem(v));
        if (selecionada != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).vaquinha == selecionada) { combo.setSelectedIndex(i); break; }
            }
        }
        atualizandoCombo = false;

        recarregarTabela();
        atualizarInfo();
        atualizarTop3();
    }

    private void recarregarTabela() {
        Vaquinha v = vaquinhaSelecionada();
        linhas = (v == null) ? List.of() : v.getContribuicoes();
        tableModel.setRowCount(0);
        for (ContribuicaoVaquinha c : linhas) {
            tableModel.addRow(new Object[]{
                c.getPessoaId(),
                nomeResolver.apply(c.getPessoaId()),
                String.format("R$ %.2f", c.getValor()),
                c.getDescricao()
            });
        }
    }

    private void atualizarInfo() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) {
            lblInfo.setText("Nenhuma vaquinha cadastrada.");
            return;
        }
        lblInfo.setText(String.format(
                "Objetivo: %s  |  Arrecadado: R$ %.2f de R$ %.2f  |  Falta: R$ %.2f",
                v.getObjetivo(), v.totalArrecadado(), v.getMeta(), v.quantoFalta()));
    }

    private void atualizarTop3() {
        topModel.setRowCount(0);
        int pos = 1;
        for (Doador d : service.top3Doadores()) {
            topModel.addRow(new Object[]{
                pos++,
                d.pessoaId(),
                nomeResolver.apply(d.pessoaId()),
                String.format("R$ %.2f", d.total())
            });
        }
    }

    private void selecionar(Vaquinha v) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).vaquinha == v) { combo.setSelectedIndex(i); break; }
        }
    }

    private Vaquinha vaquinhaSelecionada() {
        VaquinhaItem item = (VaquinhaItem) combo.getSelectedItem();
        return item == null ? null : item.vaquinha;
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

    /** Wrapper para exibir o título da vaquinha no combo. */
    private static class VaquinhaItem {
        final Vaquinha vaquinha;
        VaquinhaItem(Vaquinha vaquinha) { this.vaquinha = vaquinha; }
        @Override public String toString() { return vaquinha.getTitulo(); }
    }
}
