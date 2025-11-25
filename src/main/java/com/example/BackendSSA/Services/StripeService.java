package com.example.BackendSSA.Services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.BackendSSA.Dtos.DtoDetallePedido;
import com.example.BackendSSA.Dtos.DtoStripeCreatePaymentIntentRequest;
import com.example.BackendSSA.Dtos.DtoStripeCreatePaymentIntentResponse;
import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

@Service
public class StripeService {
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.currency:PEN}")
    private String currency;

    private final ProductoRepository productoRepository;

    public StripeService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public DtoStripeCreatePaymentIntentResponse createPaymentIntent(DtoStripeCreatePaymentIntentRequest request) throws Exception {

        // 1. Calcular total a partir de los productos de BD
        BigDecimal total = BigDecimal.ZERO;

        for (DtoDetallePedido d : request.getDetalles()) {
            ProductoEntities producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con ID: " + d.getIdProducto()));

            BigDecimal precio = producto.getPrecioBase();
            BigDecimal subTotal = precio.multiply(BigDecimal.valueOf(d.getCantidad()));
            total = total.add(subTotal);
        }

        // 2. Stripe trabaja en céntimos
        long amountInCents = total.multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", currency.toLowerCase()); // "pen"

        // Configurar automatic_payment_methods sin redirecciones
        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        automaticPaymentMethods.put("allow_redirects", "never"); // 🔴 clave

        params.put("automatic_payment_methods", automaticPaymentMethods);

        PaymentIntent paymentIntent = PaymentIntent.create(params);


        return new DtoStripeCreatePaymentIntentResponse(paymentIntent.getClientSecret());
    }
}
