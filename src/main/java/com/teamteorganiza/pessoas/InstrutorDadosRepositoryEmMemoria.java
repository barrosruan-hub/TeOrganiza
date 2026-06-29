package com.teamteorganiza.pessoas;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstrutorDadosRepositoryEmMemoria implements InstrutorDadosRepository {

    private final Map<String, InstrutorDados> dados = new HashMap<>();

    @Override
    public void salvarOuAtualizar(InstrutorDados d) { dados.put(d.getPessoaId(), d); }

    @Override
    public Optional<InstrutorDados> buscarPorPessoaId(String pessoaId) {
        return Optional.ofNullable(dados.get(pessoaId));
    }

    @Override
    public void remover(String pessoaId) { dados.remove(pessoaId); }
}
