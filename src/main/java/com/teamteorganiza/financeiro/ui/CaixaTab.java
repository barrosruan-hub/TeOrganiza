package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.VendaCaixa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class CaixaTab extends JPanel {

    private final FinanceiroService service;
    private final Function<String, String> nomeResolver;
    private final Runnable onChange;

    private final JTextField tfEvento = new JTextField(16);
    private final JLabel lblTotal = new JLabel();
    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final JTextField tfId = new JTextField(24);
    private final JTextArea taDescricao = new JTextArea(3, 24);
    private final JTextField tfValor = new JTextField(10);

    private List<VendaCaixa> linhas = List.of();

    public CaixaTab(FinanceiroService service, Function<String, String> nomeResolver, Runnable onChange) {
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
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topo.setBorder(BorderFactory.createTitledBorder("Caixa do evento"));

        topo.add(new JLabel("Evento:"));
        topo.add(tfEvento);
        JButton btnSalvarNome = new JButton("Salvar nome");
        topo.add(btnSalvarNome);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        topo.add(Box.createHorizontalStrut(16));
        topo.add(lblTotal);

        JButton btnFechar = new JButton("Fechar caixa / Novo evento");
        topo.add(Box.createHorizontalStrut(16));
        topo.add(btnFechar);

        btnSalvarNome.addActionListener(e -> {
            String nome = tfEvento.getText().trim();
            if (nome.isEmpty()) { aviso("Informe o nome do evento."); return; }
            service.setNomeEvento(nome);
            recarregar();
        });
        btnFechar.addActionListener(e -> fecharCaixa());

        return topo;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Venda"));
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
        String id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        service.registrarVenda(id, taDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma venda para editar."); return; }
        String id = lerId();
        Double valor = lerValor();
        if (id == null || valor == null) return;
        service.editarVenda(linhas.get(row).getId(), id, taDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma venda para deletar."); return; }
        service.removerVenda(linhas.get(row).getId());
        limpar();
        onChange.run();
    }

    private void fecharCaixa() {
        double total = service.totalCaixa();
        if (total <= 0) { aviso("Não há vendas no caixa para fechar."); return; }
        String nomeNovo = JOptionPane.showInputDialog(this,
                String.format("Fechar o caixa de \"%s\" (total R$ %.2f) e lançar como 1 entrada.%n"
                        + "Nome do novo evento:", service.getNomeEvento(), total),
                "Novo evento");
        if (nomeNovo == null) return;
        if (nomeNovo.trim().isEmpty()) nomeNovo = "Novo evento";
        service.fecharCaixa(nomeNovo.trim());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        VendaCaixa v = linhas.get(row);
        tfId.setText(v.getPessoaId());
        taDescricao.setText(v.getDescricao());
        tfValor.setText(String.format("%.2f", v.getValor()));
    }

    private void limpar() {
        tfId.setText("");
        taDescricao.setText("");
        tfValor.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        if (!tfEvento.isFocusOwner()) tfEvento.setText(service.getNomeEvento());
        linhas = service.getVendasCaixa();
        tableModel.setRowCount(0);
        for (VendaCaixa v : linhas) {
            tableModel.addRow(new Object[]{
                v.getPessoaId(),
                nomeResolver.apply(v.getPessoaId()),
                String.format("R$ %.2f", v.getValor()),
                v.getDescricao()
            });
        }
        lblTotal.setText(String.format("Total do caixa: R$ %.2f  (%d venda(s))",
                service.totalCaixa(), linhas.size()));
    }

    private String lerId() {
        try {
            return CampoUtil.id(tfId.getText());
        } catch (IllegalArgumentException ex) {
            aviso("ID da pessoa não informado.");
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
