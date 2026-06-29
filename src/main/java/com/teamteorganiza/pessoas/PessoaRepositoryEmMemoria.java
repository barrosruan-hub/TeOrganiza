package com.teamteorganiza.pessoas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PessoaRepositoryEmMemoria implements PessoaRepository {

    private final List<Pessoa> pessoas = new ArrayList<>();

    @Override
    public void salvar(Pessoa pessoa) {
        pessoas.removeIf(p -> p.getId().equals(pessoa.getId()));
        pessoas.add(pessoa);
    }

    @Override
    public Optional<Pessoa> buscarPorId(String id) {
        return pessoas.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @Override
    public List<Pessoa> listarTodos() { return new ArrayList<>(pessoas); }

    @Override
    public void remover(String id) { pessoas.removeIf(p -> p.getId().equals(id)); }
}
