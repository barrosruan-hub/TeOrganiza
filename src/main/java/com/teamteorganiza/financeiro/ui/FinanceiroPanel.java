package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.TipoLancamento;
import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class FinanceiroPanel extends JPanel {

    private final ExtratoTab extratoTab;
    private final MovimentacaoTab entradasTab;
    private final MovimentacaoTab despesasTab;
    private final VaquinhaTab vaquinhaTab;
    private final CaixaTab caixaTab;
    private Runnable onVoltar;

    public FinanceiroPanel(FinanceiroService service, PessoaService pessoaService) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        Function<Integer, String> nomeResolver = id -> nomeDe(pessoaService, id);
        Runnable onChange = this::recarregarTodas;

        extratoTab  = new ExtratoTab(service, nomeResolver);
        entradasTab = new MovimentacaoTab(service, nomeResolver, onChange, TipoLancamento.RECEITA);
        despesasTab = new MovimentacaoTab(service, nomeResolver, onChange, TipoLancamento.DESPESA);
        vaquinhaTab = new VaquinhaTab(service, nomeResolver, onChange);
        caixaTab    = new CaixaTab(service, nomeResolver, onChange);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Extrato",   extratoTab);
        abas.addTab("Entradas",  entradasTab);
        abas.addTab("Despesas",  despesasTab);
        abas.addTab("Vaquinhas", vaquinhaTab);
        abas.addTab("Caixa",     caixaTab);
        abas.addChangeListener(e -> recarregarTodas());

        add(abas, BorderLayout.CENTER);

        recarregarTodas();
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }

    private void recarregarTodas() {
        extratoTab.recarregar();
        entradasTab.recarregar();
        despesasTab.recarregar();
        vaquinhaTab.recarregar();
        caixaTab.recarregar();
    }

    private static String nomeDe(PessoaService pessoaService, int pessoaId) {
        if (pessoaId <= 0) return "(caixa/evento)";
        for (Pessoa p : pessoaService.listar()) {
            if (p.getId() == pessoaId) return p.getNome();
        }
        return "(id " + pessoaId + " não encontrado)";
    }
}
