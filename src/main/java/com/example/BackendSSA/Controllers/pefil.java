package com.example.BackendSSA.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/test")
public class pefil {
    // Este endpoint está protegido por defecto, ya que no es /api/auth/**
    @GetMapping("/perfil")
    public ResponseEntity<String> obtenerPerfil(Authentication authentication) {
        // Devuelve el email del usuario logueado para confirmar la autenticación
        String email = authentication.getName();
        return ResponseEntity.ok("Acceso Exitoso. Perfil de usuario: " + email);
    }
}
