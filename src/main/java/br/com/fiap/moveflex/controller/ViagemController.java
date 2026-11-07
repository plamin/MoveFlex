package br.com.fiap.moveflex.controller;

import br.com.fiap.moveflex.bo.ViagemBO;
import br.com.fiap.moveflex.model.Viagem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ViagemController {

    private final ViagemBO viagemBO;

    public ViagemController(ViagemBO viagemBO) {
        this.viagemBO = viagemBO;
    }

    @GetMapping("/viagens")
    public List<Viagem> listar() {
        return viagemBO.listar();
    }
}