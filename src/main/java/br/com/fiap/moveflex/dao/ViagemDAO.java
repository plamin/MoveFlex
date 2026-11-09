package br.com.fiap.moveflex.dao;

import br.com.fiap.moveflex.config.ConexaoOracle;
import br.com.fiap.moveflex.model.Viagem;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ViagemDAO {

    private final ConexaoOracle conexaoOracle;

    public ViagemDAO(ConexaoOracle conexaoOracle) {
        this.conexaoOracle = conexaoOracle;
    }

    public List<Viagem> listar() throws SQLException {
        String sql = """
                SELECT v.ID_VIAGEM,
                       v.DATA_VIAGEM,
                       v.HORARIO_PARTIDA,
                       v.CHEGADA_PREVISTA,
                       r.NOME AS BAIRRO
                FROM VIAGEM v
                JOIN LINHA l ON l.ID_LINHA = v.ID_LINHA
                JOIN PONTO_EMBARQUE p ON p.ID_PONTO = l.ID_PONTO_ORIGEM
                JOIN REGIAO r ON r.ID_REGIAO = p.ID_REGIAO
                ORDER BY v.DATA_VIAGEM, v.HORARIO_PARTIDA, v.ID_VIAGEM
                """;

        List<Viagem> viagens = new ArrayList<>();

        try (Connection conexao = conexaoOracle.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                LocalDate data = resultado.getDate("DATA_VIAGEM").toLocalDate();
                LocalTime hora = LocalTime.parse(resultado.getString("HORARIO_PARTIDA"));

                Viagem viagem = new Viagem(
                        resultado.getLong("ID_VIAGEM"),
                        data.atTime(hora),
                        resultado.getTimestamp("CHEGADA_PREVISTA").toLocalDateTime(),
                        resultado.getString("BAIRRO")
                );

                viagens.add(viagem);
            }
        } catch (DateTimeParseException e) {
            throw new SQLException("Horario de partida invalido no banco.", e);
        }

        return viagens;
    }
}
