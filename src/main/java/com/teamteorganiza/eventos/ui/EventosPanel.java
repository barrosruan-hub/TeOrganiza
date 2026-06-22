package com.teamteorganiza.eventos.ui;

import javax.swing.*;
import java.awt.*;

public class EventosPanel extends JPanel {

    private Runnable onVoltar;

    public EventosPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("Módulo de Eventos — em desenvolvimento", SwingConstants.CENTER);
        placeholder.setFont(new Font("SansSerif", Font.ITALIC, 18));
        placeholder.setForeground(Color.GRAY);
        add(placeholder, BorderLayout.CENTER);
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }
}
