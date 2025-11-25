package com.example.BackendSSA.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BackendSSA.Dtos.DtoStripeCreatePaymentIntentRequest;
import com.example.BackendSSA.Dtos.DtoStripeCreatePaymentIntentResponse;
import com.example.BackendSSA.Services.StripeService;

@RestController
@RequestMapping("/api/payments")
public class StripePaymentController {

    private final StripeService stripeService;

    public StripePaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody DtoStripeCreatePaymentIntentRequest request) {
        try {
            DtoStripeCreatePaymentIntentResponse response = stripeService.createPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
