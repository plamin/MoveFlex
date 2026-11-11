package br.com.fiap.moveflex.dao;

import br.com.fiap.moveflex.model.Reserva;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

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

    public Optional<Reserva> buscarPorId(Connection conexao, Long idReserva) throws SQLException {
        String sql = """
                SELECT ID_RESERVA, ID_COLABORADOR, ID_VIAGEM, DATA_SOLICITACAO, STATUS
                FROM RESERVA
                WHERE ID_RESERVA = ?
                """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idReserva);

            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }

                Reserva reserva = new Reserva(
                        resultado.getLong("ID_RESERVA"),
                        resultado.getLong("ID_COLABORADOR"),
                        resultado.getLong("ID_VIAGEM"),
                        resultado.getDate("DATA_SOLICITACAO").toLocalDate(),
                        resultado.getString("STATUS")
                );

                return Optional.of(reserva);
            }
        }
    }

    public boolean atualizarViagem(Connection conexao, Long idReserva, Long novaIdViagem)
            throws SQLException {
        String sql = """
                UPDATE RESERVA
                SET ID_VIAGEM = ?
                WHERE ID_RESERVA = ?
                AND STATUS = 'CONFIRMADA'
                """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, novaIdViagem);
            comando.setLong(2, idReserva);

            return comando.executeUpdate() == 1;
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
            Connection conexao, Long idColaborador, Long idViagem, Long idReservaIgnorada) throws SQLException {

        String sql = """
                SELECT 1
                FROM RESERVA r
                JOIN VIAGEM reservada ON reservada.ID_VIAGEM = r.ID_VIAGEM
                JOIN VIAGEM solicitada ON solicitada.ID_VIAGEM = ?
                WHERE r.ID_COLABORADOR = ?
                AND r.STATUS <> 'CANCELADA'
                AND r.ID_RESERVA <> NVL(?, -1)
                AND reservada.ID_VIAGEM <> solicitada.ID_VIAGEM
                AND reservada.SENTIDO = solicitada.SENTIDO
                AND reservada.DATA_VIAGEM >= TRUNC(solicitada.DATA_VIAGEM)
                AND reservada.DATA_VIAGEM < TRUNC(solicitada.DATA_VIAGEM) + 1
                """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, idViagem);
            comando.setLong(2, idColaborador);
            comando.setObject(3, idReservaIgnorada, java.sql.Types.NUMERIC);
            

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
}