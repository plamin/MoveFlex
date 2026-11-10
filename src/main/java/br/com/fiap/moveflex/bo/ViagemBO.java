package br.com.fiap.moveflex.bo;

import br.com.fiap.moveflex.dao.ViagemDAO;
import br.com.fiap.moveflex.exception.ConsultaViagemException;
import br.com.fiap.moveflex.model.Viagem;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.moveflex.model.OcupacaoViagem;
import java.util.Optional;

@Service
public class ViagemBO {

    private final ViagemDAO viagemDAO;

    public ViagemBO(ViagemDAO viagemDAO) {
        this.viagemDAO = viagemDAO;
    }

    public List<Viagem> listar() {
        return listar(null);
    }

    public List<Viagem> listar(LocalDate data) {
        try {
            return viagemDAO.listar(data);
        } catch (SQLException e) {
            throw new ConsultaViagemException(e);
        }
    }

    public Optional<OcupacaoViagem> consultarOcupacao(Long idViagem) {
        try {
            return viagemDAO.consultarOcupacao(idViagem);
        } catch (SQLException e) {
            throw new ConsultaViagemException(e);
        }
    }
}
