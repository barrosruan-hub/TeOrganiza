package com.teamteorganiza.pessoas;

import com.teamteorganiza.infra.SupabaseClient;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class InstrutorDadosRepositorySupabase implements InstrutorDadosRepository {

    @Override
    public void salvarOuAtualizar(InstrutorDados dados) {
        String sql = """
            INSERT INTO instrutor_dados (pessoa_id, salario, especialidades)
            VALUES (?, ?, ?)
            ON CONFLICT (pessoa_id) DO UPDATE SET
              salario        = EXCLUDED.salario,
              especialidades = EXCLUDED.especialidades
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(dados.getPessoaId()));
            ps.setDouble(2, dados.getSalario());
            ps.setString(3, dados.getEspecialidades());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar dados do instrutor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<InstrutorDados> buscarPorPessoaId(String pessoaId) {
        String sql = "SELECT pessoa_id, salario, especialidades FROM instrutor_dados WHERE pessoa_id = ?";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(pessoaId));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(new InstrutorDados(
                rs.getString("pessoa_id"),
                rs.getDouble("salario"),
                rs.getString("especialidades")
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remover(String pessoaId) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM instrutor_dados WHERE pessoa_id = ?")) {
            ps.setObject(1, UUID.fromString(pessoaId));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
