package com.teamteorganiza.pessoas;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.*;
import java.util.*;

public class TipoPessoaRepositorySupabase implements TipoPessoaRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(TipoPessoa t) {
        String sql = """
            INSERT INTO tipos_pessoa (id, organizacao_id, nome, descricao, ativo)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              nome      = EXCLUDED.nome,
              descricao = EXCLUDED.descricao,
              ativo     = EXCLUDED.ativo
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(t.getId()));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.setString(3, t.getNome());
            ps.setString(4, t.getDescricao());
            ps.setBoolean(5, t.isAtivo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar tipo de pessoa: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TipoPessoa> buscarPorId(String id) {
        return listarTodos().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<TipoPessoa> buscarPorNome(String nome) {
        return listarTodos().stream().filter(t -> t.getNome().equalsIgnoreCase(nome)).findFirst();
    }

    @Override
    public List<TipoPessoa> listarTodos() {
        String sql = "SELECT id, nome, descricao, ativo FROM tipos_pessoa WHERE organizacao_id = ? ORDER BY nome";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            List<TipoPessoa> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new TipoPessoa(
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getBoolean("ativo")
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar tipos de pessoa: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM tipos_pessoa WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
