package com.teamteorganiza.pessoas;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PessoaRepositorySupabase implements PessoaRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(Pessoa p) {
        String sql = """
            INSERT INTO pessoas (id, organizacao_id, nome, data_nascimento, cpf, telefone, email, ativo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              nome            = EXCLUDED.nome,
              data_nascimento = EXCLUDED.data_nascimento,
              telefone        = EXCLUDED.telefone,
              email           = EXCLUDED.email,
              ativo           = EXCLUDED.ativo
            """;
        try {
            Connection conn = SupabaseClient.getConnection();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, UUID.fromString(p.getId()));
                    ps.setObject(2, UUID.fromString(orgId()));
                    ps.setString(3, p.getNome());
                    ps.setDate(4, p.getDataDeNascimento() != null ? Date.valueOf(p.getDataDeNascimento()) : null);
                    ps.setString(5, p.getCpf());
                    ps.setString(6, p.getTelefone());
                    ps.setString(7, p.getEmail());
                    ps.setBoolean(8, p.isAtivo());
                    ps.executeUpdate();
                }
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM pessoa_tipos WHERE pessoa_id = ?")) {
                    del.setObject(1, UUID.fromString(p.getId()));
                    del.executeUpdate();
                }
                if (!p.getTipos().isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO pessoa_tipos (pessoa_id, tipo_id) VALUES (?, ?)")) {
                        for (TipoPessoa t : p.getTipos()) {
                            ins.setObject(1, UUID.fromString(p.getId()));
                            ins.setObject(2, UUID.fromString(t.getId()));
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pessoa: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pessoa> buscarPorId(String id) {
        String sql = BASE_SQL + " WHERE p.id = ? AND p.organizacao_id = ? ORDER BY p.nome";
        return executar(sql, id, orgId()).stream().findFirst();
    }

    @Override
    public List<Pessoa> listarTodos() {
        String sql = BASE_SQL + " WHERE p.organizacao_id = ? ORDER BY p.nome";
        return executar(sql, orgId());
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM pessoas WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String BASE_SQL = """
        SELECT p.id, p.nome, p.data_nascimento, p.cpf, p.telefone, p.email, p.ativo,
               t.id as tipo_id, t.nome as tipo_nome, t.descricao as tipo_desc, t.ativo as tipo_ativo
        FROM pessoas p
        LEFT JOIN pessoa_tipos pt ON pt.pessoa_id = p.id
        LEFT JOIN tipos_pessoa t  ON t.id = pt.tipo_id
        """;

    private List<Pessoa> executar(String sql, String... params) {
        try {
            Connection conn = SupabaseClient.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, UUID.fromString(params[i]));
                }
                ResultSet rs = ps.executeQuery();
                Map<String, Pessoa> map = new LinkedHashMap<>();
                while (rs.next()) {
                    String pessoaId = rs.getString("id");
                    if (!map.containsKey(pessoaId)) {
                        Date d = rs.getDate("data_nascimento");
                        map.put(pessoaId, new Pessoa(
                            pessoaId,
                            rs.getString("nome"),
                            d != null ? d.toLocalDate() : null,
                            rs.getString("cpf"),
                            rs.getString("telefone"),
                            rs.getString("email"),
                            rs.getBoolean("ativo")
                        ));
                    }
                    String tipoId = rs.getString("tipo_id");
                    if (tipoId != null) {
                        map.get(pessoaId).getTipos().add(new TipoPessoa(
                            tipoId,
                            rs.getString("tipo_nome"),
                            rs.getString("tipo_desc"),
                            rs.getBoolean("tipo_ativo")
                        ));
                    }
                }
                return new ArrayList<>(map.values());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pessoas: " + e.getMessage(), e);
        }
    }
}
