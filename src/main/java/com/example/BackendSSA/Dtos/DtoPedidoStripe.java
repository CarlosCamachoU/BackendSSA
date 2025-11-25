package com.example.BackendSSA.Dtos;

import java.util.List;

import lombok.Data;

@Data
public class DtoPedidoStripe {
    
    private String direccionEnvio;
    private String dni;
    private String metodoPago;      // normalmente "TARJETA_STRIPE"
    private String paymentIntentId; // pi_xxx de Stripe

    private List<DtoDetallePedido> detalles;
}
