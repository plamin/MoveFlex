package br.com.fiap.moveflex.controller;

import br.com.fiap.moveflex.bo.ReservaBO;
import br.com.fiap.moveflex.model.Reserva;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaBO reservaBO;

    public ReservaController(ReservaBO reservaBO) {
        this.reservaBO = reservaBO;
    }

    public record PedidoReserva(Long idColaborador, Long idViagem) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva criar(@RequestBody PedidoReserva pedido) {
        return reservaBO.reservar(pedido.idColaborador(), pedido.idViagem());
    }

    public record PedidoTrocaViagem(Long idViagem) {}

    @PutMapping("/{id}")
    public Reserva trocarViagem(@PathVariable("id") Long idReserva,
                                @RequestBody PedidoTrocaViagem pedido) {
        return reservaBO.trocarViagem(idReserva, pedido.idViagem());
    }
}