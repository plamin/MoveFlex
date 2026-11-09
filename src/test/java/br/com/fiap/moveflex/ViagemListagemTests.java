package br.com.fiap.moveflex;

import br.com.fiap.moveflex.bo.ViagemBO;
import br.com.fiap.moveflex.config.ConexaoOracle;
import br.com.fiap.moveflex.controller.ViagemController;
import br.com.fiap.moveflex.dao.ViagemDAO;
import br.com.fiap.moveflex.exception.TratamentoExcecoes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ViagemListagemTests {
    private ConexaoOracle oracle;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet result;
    private MockMvc mvc;

    @BeforeEach
    void preparar() throws Exception {
        oracle = mock(ConexaoOracle.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        result = mock(ResultSet.class);
        when(oracle.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(result);
        mvc = MockMvcBuilders.standaloneSetup(new ViagemController(new ViagemBO(new ViagemDAO(oracle))))
                .setControllerAdvice(new TratamentoExcecoes()).build();
    }

    @Test
    void preservaContratoECombinaDataComHora() throws Exception {
        when(result.next()).thenReturn(true, false);
        when(result.getLong("ID_VIAGEM")).thenReturn(1L);
        when(result.getDate("DATA_VIAGEM")).thenReturn(Date.valueOf("2025-11-03"));
        when(result.getString("HORARIO_PARTIDA")).thenReturn("23:30");
        when(result.getTimestamp("CHEGADA_PREVISTA"))
                .thenReturn(Timestamp.valueOf("2025-11-04 00:11:00"));
        when(result.getString("BAIRRO")).thenReturn("Mooca");

        mvc.perform(get("/viagens")).andExpect(status().isOk()).andExpect(content().json("""
                [{"id":1,"dataHoraSaida":"2025-11-03T23:30:00",
                  "dataHoraChegada":"2025-11-04T00:11:00","bairro":"Mooca"}]
                """));
        verificarFechamento();
    }

    @Test
    void semViagensRetornaListaVazia() throws Exception {
        mvc.perform(get("/viagens")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verificarFechamento();
    }

    @Test
    void falhaDeConexaoRetornaErroSemDetalhesInternos() throws Exception {
        when(oracle.getConnection()).thenThrow(new SQLException("credenciais e detalhes internos"));
        verificarErro();
    }

    @Test
    void falhaDeConsultaFechaRecursosERetornaErro() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("SQL interno"));
        verificarErro();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void horarioInvalidoNoBancoRetornaErroEFechaRecursos() throws Exception {
        when(result.next()).thenReturn(true);
        when(result.getDate("DATA_VIAGEM")).thenReturn(Date.valueOf("2025-11-03"));
        when(result.getString("HORARIO_PARTIDA")).thenReturn("29:00");
        verificarErro();
        verificarFechamento();
    }

    private void verificarErro() throws Exception {
        mvc.perform(get("/viagens")).andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"status\":500,\"mensagem\":\"Falha ao consultar as viagens.\"}"));
    }

    private void verificarFechamento() throws Exception {
        verify(result).close();
        verify(statement).close();
        verify(connection).close();
    }
}
