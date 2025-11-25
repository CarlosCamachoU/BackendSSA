package com.example.BackendSSA.Dtos;

import java.util.List;
import lombok.Data;

@Data
public class DtoPedido {

    private String direccionEnvio;
    private String dni;

    // Método de pago: TARJETA_STRIPE, EFECTIVO, etc.
    private String metodoPago;

    // Aquí guardaremos el paymentIntentId de Stripe
    private String idTransaccionCulqi; // lo usas como idTransaccionStripe internamente

    private List<DtoDetallePedido> detalles;
}
