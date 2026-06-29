package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;

import javax.swing.*;
import java.awt.*;

public class EstoquePanel extends JPanel {

    private final ProdutosTab produtosTab;
    private final EntradaTab entradaTab;
    private final BaixaTab baixaTab;
    private final ReposicaoTab reposicaoTab;
    private final MovimentosTab movimentosTab;
    private Runnable onVoltar;

    public EstoquePanel(EstoqueService service) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        Runnable onChange = this::recarregarTodas;

        produtosTab   = new ProdutosTab(service, onChange);
        entradaTab    = new EntradaTab(service, onChange);
        baixaTab      = new BaixaTab(service, onChange);
        reposicaoTab  = new ReposicaoTab(service);
        movimentosTab = new MovimentosTab(service);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Produtos",   produtosTab);
        abas.addTab("Entrada",    entradaTab);
        abas.addTab("Baixa",      baixaTab);
        abas.addTab("Reposição",  reposicaoTab);
        abas.addTab("Movimentos", movimentosTab);
        abas.addChangeListener(e -> recarregarTodas());

        add(abas, BorderLayout.CENTER);

        recarregarTodas();
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }

    private void recarregarTodas() {
        produtosTab.recarregar();
        entradaTab.recarregar();
        baixaTab.recarregar();
        reposicaoTab.recarregar();
        movimentosTab.recarregar();
    }
}
