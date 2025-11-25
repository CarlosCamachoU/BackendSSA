package com.example.BackendSSA.Dtos;

import java.util.List;

import lombok.Data;

@Data
public class DtoStripeCreatePaymentIntentRequest {

    // No es obligatorio mandar amount desde el front, lo calculamos en backend con los productos
    private List<DtoDetallePedido> detalles;
    
}
