package com.teamteorganiza.pessoas;

import java.util.UUID;

public class TipoPessoa {

    private final String id;
    private String nome;
    private String descricao;
    private boolean ativo;

    public TipoPessoa(String nome, String descricao) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.descricao = descricao != null ? descricao : "";
        this.ativo = true;
    }

    public TipoPessoa(String id, String nome, String descricao, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao != null ? descricao : "";
        this.ativo = ativo;
    }

    public String getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao != null ? descricao : ""; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() { return nome; }
}
