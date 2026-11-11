package br.com.fiap.moveflex.bo;

import br.com.fiap.moveflex.config.ConexaoOracle;
import br.com.fiap.moveflex.dao.ReservaDAO;
import br.com.fiap.moveflex.dao.ViagemDAO;
import br.com.fiap.moveflex.exception.ConsultaViagemException;
import br.com.fiap.moveflex.model.OcupacaoViagem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Connection;
import br.com.fiap.moveflex.model.Reserva;
import java.time.LocalDate;

import java.sql.SQLException;

@Service
public class ReservaBO {

    private final ViagemDAO viagemDAO;
    private final ReservaDAO reservaDAO;
    private final ConexaoOracle conexaoOracle;

    public ReservaBO(ViagemDAO viagemDAO, ReservaDAO reservaDAO, ConexaoOracle conexaoOracle) {
        this.viagemDAO = viagemDAO;
        this.reservaDAO = reservaDAO;
        this.conexaoOracle = conexaoOracle;
    }

    public Reserva reservar(Long idColaborador, Long idViagem) {
        if (idColaborador == null || idColaborador <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe um ID de colaborador válido."
            );
        }

        if (idViagem == null || idViagem <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe um ID de viagem válido."
            );
        }
        try (Connection conexao = conexaoOracle.getConnection()) {
            if (!reservaDAO.colaboradorExiste(conexao, idColaborador)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Colaborador não encontrado."
                );
            }

            if (reservaDAO.existeReserva(conexao, idColaborador, idViagem)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Já existe uma reserva deste colaborador para esta viagem"
                );
            }

            if (reservaDAO.existeReservaNoMesmoDiaESentido(conexao, idColaborador, idViagem)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O colaborador já possui uma reserva nesse sentido para este dia."
                );
            }
            validarDisponibilidade(idViagem);
            return reservaDAO.inserir(conexao, idColaborador, idViagem, LocalDate.now());
        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao processar a reserva.", e
            );
        }
    }


    private void validarDisponibilidade(Long idViagem) {
        try {
            OcupacaoViagem ocupacao = viagemDAO.consultarOcupacao(idViagem)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Viagem não encontrada."
                    ));

            if (ocupacao.getVagasDisponiveis() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Viagem sem vagas disponíveis."
                );
            }
        } catch (SQLException e) {
            throw new ConsultaViagemException(e);
        }
    }
}