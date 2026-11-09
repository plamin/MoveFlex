package br.com.fiap.moveflex.controller;

import br.com.fiap.moveflex.bo.ViagemBO;
import br.com.fiap.moveflex.model.Viagem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import java.util.List;

@RestController
public class ViagemController {

    private final ViagemBO viagemBO;

    public ViagemController(ViagemBO viagemBO) {
        this.viagemBO = viagemBO;
    }

    @GetMapping("/viagens")
    public List<Viagem> listar(@RequestParam(name = "data", required = false) String data) {
        LocalDate filtro = null;
        if (data != null) {
            try {
                if (!data.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                    throw new DateTimeParseException("Formato invalido", data, 0);
                }
                filtro = LocalDate.parse(data);
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Data invalida. Use yyyy-MM-dd.");
            }
        }
        return viagemBO.listar(filtro);
    }
}
