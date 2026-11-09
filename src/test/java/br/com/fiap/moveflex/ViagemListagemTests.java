package br.com.fiap.moveflex;

import br.com.fiap.moveflex.bo.ViagemBO;
import br.com.fiap.moveflex.config.ConexaoOracle;
import br.com.fiap.moveflex.controller.ViagemController;
import br.com.fiap.moveflex.dao.ViagemDAO;
import br.com.fiap.moveflex.exception.TratamentoExcecoes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2025-11-03")
    void preservaContratoECombinaDataComHora(String data) throws Exception {
        when(result.next()).thenReturn(true, false);
        when(result.getLong("ID_VIAGEM")).thenReturn(1L);
        when(result.getDate("DATA_VIAGEM")).thenReturn(Date.valueOf("2025-11-03"));
        when(result.getString("HORARIO_PARTIDA")).thenReturn("23:30");
        when(result.getTimestamp("CHEGADA_PREVISTA"))
                .thenReturn(Timestamp.valueOf("2025-11-04 00:11:00"));
        when(result.getString("BAIRRO")).thenReturn("Mooca");

        var request = get("/viagens");
        if (data != null) request.param("data", data);
        mvc.perform(request).andExpect(status().isOk()).andExpect(content().json("""
                [{"id":1,"dataHoraSaida":"2025-11-03T23:30:00",
                  "dataHoraChegada":"2025-11-04T00:11:00","bairro":"Mooca"}]
                """));
        verificarFechamento();
        verificarFiltro(data);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2025-11-03")
    void semViagensRetornaListaVazia(String data) throws Exception {
        var request = get("/viagens");
        if (data != null) request.param("data", data);
        mvc.perform(request).andExpect(status().isOk()).andExpect(content().json("[]"));
        verificarFechamento();
        verificarFiltro(data);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-02-29", "2025-11-31", "03/11/2025", "abc", "", "2025-1-03"})
    void dataInvalidaRetorna400SemConsultarBanco(String data) throws Exception {
        mvc.perform(get("/viagens").param("data", data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("mensagem").value("Data invalida. Use yyyy-MM-dd."));
        verifyNoInteractions(oracle, connection, statement, result);
    }

    private void verificarFiltro(String data) throws Exception {
        var sql = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sql.capture());
        String consulta = sql.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(consulta.endsWith(
                "ORDER BY v.DATA_VIAGEM, v.HORARIO_PARTIDA, v.ID_VIAGEM"));
        org.junit.jupiter.api.Assertions.assertTrue(consulta.contains("JOIN LINHA l ON l.ID_LINHA = v.ID_LINHA"));
        org.junit.jupiter.api.Assertions.assertTrue(consulta.contains("JOIN PONTO_EMBARQUE p ON p.ID_PONTO = l.ID_PONTO_ORIGEM"));
        org.junit.jupiter.api.Assertions.assertTrue(consulta.contains("JOIN REGIAO r ON r.ID_REGIAO = p.ID_REGIAO"));
        if (data == null) {
            org.junit.jupiter.api.Assertions.assertFalse(consulta.contains("WHERE"));
            verify(statement, never()).setDate(anyInt(), any());
        } else {
            org.junit.jupiter.api.Assertions.assertTrue(consulta.contains("WHERE v.DATA_VIAGEM >= ? AND v.DATA_VIAGEM < ?"));
            var ordem = inOrder(statement);
            ordem.verify(statement).setDate(1, Date.valueOf(data));
            ordem.verify(statement).setDate(2, Date.valueOf(java.time.LocalDate.parse(data).plusDays(1)));
            ordem.verify(statement).executeQuery();
        }
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
