package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.model.Produto;

import javax.swing.*;
import java.awt.*;

/** Mostra o produto no combo como "Nome (qtd un)" em vez do toString completo. */
class ProdutoRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Produto p) {
            setText(String.format("%s (%.2f %s)", p.getNome(), p.getQuantidade(), p.getUnidade()));
        }
        return this;
    }
}
