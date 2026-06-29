package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Aba só-leitura com os produtos que estão abaixo (ou no limite) do estoque mínimo. */
public class ReposicaoTab extends JPanel {

    private final EstoqueService service;

    private final DefaultTableModel tableModel;
    private final JLabel resumo = new JLabel();

    public ReposicaoTab(EstoqueService service) {
        this.service = service;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] colunas = {"ID", "Nome", "Categoria", "Qtd atual", "Mínimo", "Un."};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabela = new JTable(tableModel);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        resumo.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(resumo, BorderLayout.SOUTH);
    }

    public void recarregar() {
        var abaixo = service.listarAbaixoDoEstoqueMinimo();
        tableModel.setRowCount(0);
        for (Produto p : abaixo) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                String.format("%.2f", p.getQuantidade()),
                String.format("%.2f", p.getEstoqueMinimo()),
                p.getUnidade()
            });
        }
        resumo.setText(abaixo.isEmpty()
                ? "Nenhum produto abaixo do estoque mínimo."
                : abaixo.size() + " produto(s) precisando de reposição.");
    }
}
