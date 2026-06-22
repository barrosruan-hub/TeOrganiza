package com.teamteorganiza.ui;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {

    private Runnable onPessoas;
    private Runnable onFinanceiro;
    private Runnable onEstoque;
    private Runnable onEventos;

    public HomePanel() {
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("TeOrganiza", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        titulo.setBorder(BorderFactory.createEmptyBorder(50, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel botoesPanel = new JPanel(new GridLayout(1, 4, 24, 0));
        botoesPanel.setBorder(BorderFactory.createEmptyBorder(80, 60, 120, 60));

        botoesPanel.add(criarBotao("Pessoas",    () -> { if (onPessoas   != null) onPessoas.run();    }));
        botoesPanel.add(criarBotao("Financeiro", () -> { if (onFinanceiro != null) onFinanceiro.run(); }));
        botoesPanel.add(criarBotao("Estoque",    () -> { if (onEstoque   != null) onEstoque.run();    }));
        botoesPanel.add(criarBotao("Eventos",    () -> { if (onEventos   != null) onEventos.run();    }));

        add(botoesPanel, BorderLayout.CENTER);
    }

    private JButton criarBotao(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 22));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
    }

    public void setOnPessoas(Runnable r)    { this.onPessoas    = r; }
    public void setOnFinanceiro(Runnable r) { this.onFinanceiro = r; }
    public void setOnEstoque(Runnable r)    { this.onEstoque    = r; }
    public void setOnEventos(Runnable r)    { this.onEventos    = r; }
}
