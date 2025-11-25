package com.example.BackendSSA.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoPedidoRespuesta;
import com.example.BackendSSA.Services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")   
public class PedidoHistorialController {

    
    private final PedidoService pedidoService;

    public PedidoHistorialController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // GET /api/pedidos/mis-pedidos
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<DtoPedidoRespuesta>> listarMisPedidos() {
        List<DtoPedidoRespuesta> pedidos = pedidoService.listarPedidosUsuarioActual();
        return ResponseEntity.ok(pedidos);
    }

    // GET /api/pedidos/mis-pedidos/{id}
    @GetMapping("/mis-pedidos/{idPedido}")
    public ResponseEntity<DtoPedidoRespuesta> obtenerDetallePedido(@PathVariable Integer idPedido) {
        DtoPedidoRespuesta pedido = pedidoService.obtenerPedidoUsuarioActualPorId(idPedido);
        return ResponseEntity.ok(pedido);
    }
    
}
