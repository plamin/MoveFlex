package br.com.fiap.moveflex;

import org.junit.jupiter.api.Test;
import br.com.fiap.moveflex.dao.ViagemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ViagemOracleIT {
    @Autowired
    private ViagemDAO viagemDAO;

    @Test
    void consultaDaoNoOracleReal() throws Exception {
        try {
            assertNotNull(viagemDAO.listar());
        } catch (java.sql.SQLException e) {
            fail("Consulta Oracle falhou: codigo=" + e.getErrorCode() + ", SQLState=" + e.getSQLState());
        }
    }

    @LocalServerPort
    private int port;

    @Test
    void getViagensConsultaOracleReal() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/viagens"))
                    .timeout(Duration.ofSeconds(45)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().startsWith("["));
            assertTrue(response.body().endsWith("]"));
        }
    }
}
