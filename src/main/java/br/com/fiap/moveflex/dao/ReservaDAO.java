package br.com.fiap.moveflex.dao;

import br.com.fiap.moveflex.model.Reserva;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Repository
public class ReservaDAO {

    public Reserva inserir(Connection conexao, Long idColaborador,
                           Long idViagem, LocalDate dataSolicitacao) throws SQLException {
        String sql = """
                INSERT INTO RESERVA
                    (ID_RESERVA, ID_COLABORADOR, ID_VIAGEM, DATA_SOLICITACAO, STATUS)
                VALUES
                    (SEQ_RESERVA.NEXTVAL, ?, ?, ?, 'CONFIRMADA')
                """;

        try (PreparedStatement comando = conexao.prepareStatement(
                sql, new String[]{"ID_RESERVA"})) {

            comando.setLong(1, idColaborador);
            comando.setLong(2, idViagem);
            comando.setDate(3, Date.valueOf(dataSolicitacao));
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O Oracle não retornou o ID");
                }

                return new Reserva(
                        chaves.getLong(1),
                        idColaborador,
                        idViagem,
                        dataSolicitacao,
                        "CONFIRMADA"
                );
            }
        }
    }

    public boolean colaboradorExiste(Connection conexao, Long idColaborador) throws SQLException {
        String sql = "SELECT 1 FROM COLABORADOR WHERE ID_COLABORADOR = ?";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idColaborador);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public boolean existeReserva(Connection conexao, Long idColaborador, Long idViagem)
        throws SQLException {
            String sql = """
                    SELECT 1
                    FROM RESERVA
                    WHERE ID_COLABORADOR = ?
                    AND ID_VIAGEM = ?
                    """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idColaborador);
            comando.setLong(2, idViagem);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
    public boolean existeReservaNoMesmoDiaESentido(
            Connection conexao, Long idColaborador, Long idViagem) throws SQLException {

        String sql = """
                SELECT 1
                FROM RESERVA r
                JOIN VIAGEM reservada ON reservada.ID_VIAGEM = r.ID_VIAGEM
                JOIN VIAGEM solicitada ON solicitada.ID_VIAGEM = ?
                WHERE r.ID_COLABORADOR = ?
                AND r.STATUS <> 'CANCELADA'
                AND reservada.ID_VIAGEM <> solicitada.ID_VIAGEM
                AND reservada.SENTIDO = solicitada.SENTIDO
                AND reservada.DATA_VIAGEM >= TRUNC(solicitada.DATA_VIAGEM)
                AND reservada.DATA_VIAGEM < TRUNC(solicitada.DATA_VIAGEM) + 1
                """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idViagem);
            comando.setLong(2, idColaborador);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
}