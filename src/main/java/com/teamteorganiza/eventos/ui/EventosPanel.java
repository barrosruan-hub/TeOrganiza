package com.teamteorganiza.eventos.ui;

import com.teamteorganiza.eventos.EventosService;
import com.teamteorganiza.eventos.model.Compromisso;
import com.teamteorganiza.eventos.model.TipoCompromisso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventosPanel extends JPanel {

    private final EventosService service;
    private Runnable onVoltar;

    private final DefaultTableModel tableModel;
    private final JTable tabela;

    private final JTextField txtTitulo = new JTextField(16);
    private final JComboBox<TipoCompromisso> cbTipo = new JComboBox<>(TipoCompromisso.values());
    private final JComboBox<String> cbCategoria = new JComboBox<>();
    private final JTextField txtData = new JTextField(LocalDate.now().toString());
    private final JTextField txtHorario = new JTextField(8);
    private final JTextField txtLocal = new JTextField(16);
    private final JTextField txtResponsavel = new JTextField(16);
    private final JTextField txtDescricao = new JTextField(16);

    private List<Compromisso> linhas = List.of();

    public EventosPanel(EventosService service) {
        this.service = service;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        String[] colunas = {"ID", "Título", "Tipo", "Categoria", "Data", "Local", "Responsável"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });

        cbTipo.addActionListener(e -> atualizarCategorias());
        atualizarCategorias();

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        painel.add(montarFormulario(), BorderLayout.SOUTH);
        add(painel, BorderLayout.CENTER);

        recarregar();
    }

    private JPanel montarFormulario() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Compromisso"));

        formulario.add(new JLabel("Título:"));           formulario.add(txtTitulo);
        formulario.add(new JLabel("Tipo:"));             formulario.add(cbTipo);
        formulario.add(new JLabel("Categoria:"));        formulario.add(cbCategoria);
        formulario.add(new JLabel("Data (AAAA-MM-DD):")); formulario.add(txtData);
        formulario.add(new JLabel("Horário:"));          formulario.add(txtHorario);
        formulario.add(new JLabel("Local:"));            formulario.add(txtLocal);
        formulario.add(new JLabel("Responsável:"));      formulario.add(txtResponsavel);
        formulario.add(new JLabel("Descrição:"));        formulario.add(txtDescricao);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnCriar = new JButton("Cadastrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDeletar = new JButton("Deletar");
        JButton btnLimpar = new JButton("Limpar");
        botoes.add(btnCriar); botoes.add(btnEditar); botoes.add(btnDeletar); botoes.add(btnLimpar);

        btnCriar.addActionListener(e -> criar());
        btnEditar.addActionListener(e -> editar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limpar());

        JPanel sul = new JPanel(new BorderLayout(0, 6));
        sul.add(formulario, BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);
        return sul;
    }

    private void atualizarCategorias() {
        cbCategoria.removeAllItems();
        TipoCompromisso tipo = (TipoCompromisso) cbTipo.getSelectedItem();
        if (tipo == null) return;
        for (String categoria : tipo.categorias()) {
            cbCategoria.addItem(categoria);
        }
        cbCategoria.setEnabled(cbCategoria.getItemCount() > 0);
    }

    private void criar() {
        LocalDate data = lerData();
        if (data == null) return;
        try {
            service.cadastrar(
                txtTitulo.getText().trim(),
                (TipoCompromisso) cbTipo.getSelectedItem(),
                categoriaSelecionada(),
                data,
                txtHorario.getText().trim(),
                txtLocal.getText().trim(),
                txtResponsavel.getText().trim(),
                txtDescricao.getText().trim()
            );
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        recarregar();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um compromisso para editar."); return; }
        LocalDate data = lerData();
        if (data == null) return;
        try {
            service.editar(
                linhas.get(row).getId(),
                txtTitulo.getText().trim(),
                (TipoCompromisso) cbTipo.getSelectedItem(),
                categoriaSelecionada(),
                data,
                txtHorario.getText().trim(),
                txtLocal.getText().trim(),
                txtResponsavel.getText().trim(),
                txtDescricao.getText().trim()
            );
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        recarregar();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um compromisso para deletar."); return; }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Excluir o compromisso \"" + linhas.get(row).getTitulo() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (opcao != JOptionPane.YES_OPTION) return;
        service.remover(linhas.get(row).getId());
        limpar();
        recarregar();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        Compromisso c = linhas.get(row);
        txtTitulo.setText(c.getTitulo());
        cbTipo.setSelectedItem(c.getTipo());
        atualizarCategorias();
        cbCategoria.setSelectedItem(c.getCategoria());
        txtData.setText(c.getData() != null ? c.getData().toString() : "");
        txtHorario.setText(c.getHorario());
        txtLocal.setText(c.getLocal());
        txtResponsavel.setText(c.getResponsavel());
        txtDescricao.setText(c.getDescricao());
    }

    private void limpar() {
        txtTitulo.setText("");
        cbTipo.setSelectedIndex(0);
        atualizarCategorias();
        txtData.setText(LocalDate.now().toString());
        txtHorario.setText("");
        txtLocal.setText("");
        txtResponsavel.setText("");
        txtDescricao.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        linhas = service.listar();
        tableModel.setRowCount(0);
        for (Compromisso c : linhas) {
            tableModel.addRow(new Object[]{
                c.getId(),
                c.getTitulo(),
                c.getTipo(),
                c.getCategoria(),
                c.getData(),
                c.getLocal(),
                c.getResponsavel()
            });
        }
    }

    private String categoriaSelecionada() {
        Object sel = cbCategoria.getSelectedItem();
        return sel != null ? sel.toString() : "";
    }

    private LocalDate lerData() {
        try {
            return LocalDate.parse(txtData.getText().trim());
        } catch (DateTimeParseException ex) {
            aviso("Formato de data inválido! Use o padrão AAAA-MM-DD.");
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }
}
