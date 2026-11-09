package br.com.fiap.moveflex.exception;

public class ConsultaViagemException extends RuntimeException {

    public ConsultaViagemException(Throwable causa) {
        super("Falha ao consultar as viagens.", causa);
    }
}
