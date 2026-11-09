package br.com.fiap.moveflex.bo;

import br.com.fiap.moveflex.dao.ViagemDAO;
import br.com.fiap.moveflex.exception.ConsultaViagemException;
import br.com.fiap.moveflex.model.Viagem;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ViagemBO {

    private final ViagemDAO viagemDAO;

    public ViagemBO(ViagemDAO viagemDAO) {
        this.viagemDAO = viagemDAO;
    }

    public List<Viagem> listar() {
        try {
            return viagemDAO.listar();
        } catch (SQLException e) {
            throw new ConsultaViagemException(e);
        }
    }
}
