package br.com.fiap.moveflex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratamentoExcecoes {

    @ExceptionHandler(ConsultaViagemException.class)
    public ResponseEntity<ErroResposta> tratarConsultaViagem(ConsultaViagemException excecao) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResposta(500, "Falha ao consultar as viagens."));
    }

    public record ErroResposta(int status, String mensagem) {
    }
}
