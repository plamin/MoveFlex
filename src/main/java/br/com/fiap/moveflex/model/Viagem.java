package br.com.fiap.moveflex.model;

import java.time.LocalDateTime;

public class Viagem {

    private final Long id;
    private final LocalDateTime dataHoraSaida;
    private final LocalDateTime dataHoraChegada;
    private final String bairro;

    public Viagem(Long id, LocalDateTime dataHoraSaida, LocalDateTime dataHoraChegada, String bairro) {
        this.id = id;
        this.dataHoraSaida = dataHoraSaida;
        this.dataHoraChegada = dataHoraChegada;
        this.bairro = bairro;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public LocalDateTime getDataHoraChegada() {
        return dataHoraChegada;
    }

    public String getBairro() {
        return bairro;
    }
}