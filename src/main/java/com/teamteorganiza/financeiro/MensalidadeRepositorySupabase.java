package com.teamteorganiza.financeiro;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.financeiro.model.Mensalidade;
import com.teamteorganiza.financeiro.model.StatusMensalidade;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MensalidadeRepositorySupabase implements MensalidadeRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(Mensalidade m) {
        String sql = """
            INSERT INTO mensalidades (id, organizacao_id, pessoa_id, mes_referencia, vencimento, valor, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              status = EXCLUDED.status,
              valor  = EXCLUDED.valor
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(m.getId()));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.setObject(3, m.getPessoaId() != null && !m.getPessoaId().isEmpty()
                ? UUID.fromString(m.getPessoaId()) : null);
            ps.setString(4, m.getMesReferencia());
            ps.setDate(5, Date.valueOf(m.getVencimento()));
            ps.setDouble(6, m.getValor());
            ps.setString(7, m.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar mensalidade: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Mensalidade> buscarPorId(String id) {
        String sql = "SELECT * FROM mensalidades WHERE id = ? AND organizacao_id = ?";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Mensalidade> listarTodos() {
        String sql = "SELECT * FROM mensalidades WHERE organizacao_id = ? ORDER BY vencimento";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            List<Mensalidade> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM mensalidades WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Mensalidade mapear(ResultSet rs) throws SQLException {
        String pessoaId = rs.getString("pessoa_id");
        return new Mensalidade(
            rs.getString("id"),
            pessoaId != null ? pessoaId : "",
            rs.getString("mes_referencia"),
            rs.getDouble("valor"),
            rs.getDate("vencimento").toLocalDate(),
            StatusMensalidade.valueOf(rs.getString("status"))
        );
    }
}
