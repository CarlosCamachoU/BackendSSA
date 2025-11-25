package com.example.BackendSSA.Controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoDetallePedidoRespuesta;
import com.example.BackendSSA.Dtos.DtoPedidoRespuesta;
import com.example.BackendSSA.Dtos.DtoPedidoStripe;
import com.example.BackendSSA.Entities.DetallePedidoEntities;
import com.example.BackendSSA.Entities.PedidoEntities;
import com.example.BackendSSA.Services.StripeCheckoutService;

@RestController
@RequestMapping("/api/checkout/stripe")
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

    public StripeCheckoutController(StripeCheckoutService stripeCheckoutService) {
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmarYCrearPedido(@RequestBody DtoPedidoStripe request) {
        try {
            PedidoEntities pedido = stripeCheckoutService.confirmarPagoYCrearPedido(request);

            DtoPedidoRespuesta respuesta = mapearAPedidoRespuesta(pedido);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private DtoPedidoRespuesta mapearAPedidoRespuesta(PedidoEntities pedido) {
        List<DtoDetallePedidoRespuesta> detallesDto = pedido.getDetalles()
            .stream()
            .map(this::mapearDetalle)
            .collect(Collectors.toList());

        return new DtoPedidoRespuesta(
            pedido.getIdPedido(),
            pedido.getNumeroPedidoCliente(),
            pedido.getFechaPedido(),
            pedido.getTotal(),
            pedido.getDireccionEnvio(),
            pedido.getDni(),
            pedido.getMetodoPago(),
            pedido.getIdTransaccionCulqi(), // aquí estás guardando el paymentIntentId
            detallesDto
        );
    }

    private DtoDetallePedidoRespuesta mapearDetalle(DetallePedidoEntities detalle) {
        return new DtoDetallePedidoRespuesta(
            detalle.getProducto().getIdProducto(),
            detalle.getProducto().getNombre(),
            detalle.getCantidad(),
            detalle.getPrecioUnitarioCompra()
        );
    }
}
