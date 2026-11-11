package br.com.fiap.moveflex.model;

import java.time.LocalDate;

public class Reserva {

    private final Long id;
    private final Long idColaborador;
    private final Long idViagem;
    private final LocalDate dataSolicitacao;
    private final String status;

    public Reserva(Long id, Long idColaborador, Long idViagem,
                   LocalDate dataSolicitacao, String status) {
        this.id = id;
        this.idColaborador = idColaborador;
        this.idViagem = idViagem;
        this.dataSolicitacao = dataSolicitacao;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getIdColaborador() {
        return idColaborador;
    }

    public Long getIdViagem() {
        return idViagem;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public String getStatus() {
        return status;
    }
}