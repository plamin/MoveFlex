package br.com.fiap.moveflex;

import br.com.fiap.moveflex.config.ConexaoOracle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ConexaoOracleIT {

    @Autowired
    private ConexaoOracle conexaoOracle;

    @Test
    void deveConectarAoOracle() throws Exception {
        try (Connection conexao = conexaoOracle.getConnection();
             PreparedStatement consulta = conexao.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet resultado = consulta.executeQuery()) {

            assertTrue(resultado.next());
            assertEquals(1, resultado.getInt(1));
        }
    }
}