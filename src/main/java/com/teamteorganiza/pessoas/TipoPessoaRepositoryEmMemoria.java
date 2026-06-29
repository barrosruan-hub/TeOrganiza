package com.teamteorganiza.pessoas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoPessoaRepositoryEmMemoria implements TipoPessoaRepository {

    private final List<TipoPessoa> tipos = new ArrayList<>();

    @Override
    public void salvar(TipoPessoa tipo) {
        tipos.removeIf(t -> t.getId().equals(tipo.getId()));
        tipos.add(tipo);
    }

    @Override
    public Optional<TipoPessoa> buscarPorId(String id) {
        return tipos.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    @Override
    public List<TipoPessoa> listarTodos() { return new ArrayList<>(tipos); }

    @Override
    public void remover(String id) { tipos.removeIf(t -> t.getId().equals(id)); }

    @Override
    public Optional<TipoPessoa> buscarPorNome(String nome) {
        return tipos.stream().filter(t -> t.getNome().equalsIgnoreCase(nome)).findFirst();
    }
}
