package br.com.fiap.moveflex.model;

public class OcupacaoViagem {

    private final Long idViagem;
    private final int capacidade;
    private final int lugaresOcupados;

    public OcupacaoViagem(Long idViagem, int capacidade, int lugaresOcupados) {
        this.idViagem = idViagem;
        this.capacidade = capacidade;
        this.lugaresOcupados = lugaresOcupados;
    }

    public Long getIdViagem() {
        return idViagem;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getLugaresOcupados() {
        return lugaresOcupados;
    }

    public int getVagasDisponiveis() {
        return capacidade - lugaresOcupados;
    }
}