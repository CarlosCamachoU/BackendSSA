package com.example.BackendSSA.Services;

import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.transaction.annotation.Transactional;

import com.example.BackendSSA.Dtos.DtoPedido;
import com.example.BackendSSA.Dtos.DtoPedidoStripe;
import com.example.BackendSSA.Entities.PedidoEntities;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

import jakarta.annotation.PostConstruct;

@Service
public class StripeCheckoutService {
     @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final PedidoService pedidoService;

    public StripeCheckoutService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Verifica el PaymentIntent en Stripe y, si está succeeded,
     * crea el Pedido en BD usando PedidoService.
     */
    @Transactional
    public PedidoEntities confirmarPagoYCrearPedido(DtoPedidoStripe dto) throws Exception {

        if (dto.getPaymentIntentId() == null || dto.getPaymentIntentId().isBlank()) {
            throw new IllegalArgumentException("paymentIntentId es obligatorio");
        }

        PaymentIntent paymentIntent = PaymentIntent.retrieve(dto.getPaymentIntentId());

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new IllegalStateException("El pago aún no está aprobado. Estado: " + paymentIntent.getStatus());
        }

        // Mapear DtoPedidoStripe -> DtoPedido (el que ya usa PedidoService)
        DtoPedido pedidoDto = new DtoPedido();
        pedidoDto.setDireccionEnvio(dto.getDireccionEnvio());
        pedidoDto.setDni(dto.getDni());
        pedidoDto.setMetodoPago(
                dto.getMetodoPago() != null && !dto.getMetodoPago().isBlank()
                        ? dto.getMetodoPago()
                        : "TARJETA_STRIPE"
        );
        // Reutilizamos el campo idTransaccionCulqi para guardar el paymentIntentId de Stripe
        pedidoDto.setIdTransaccionCulqi(dto.getPaymentIntentId());
        pedidoDto.setDetalles(dto.getDetalles());

        return pedidoService.crearPedido(pedidoDto);
    }
}
