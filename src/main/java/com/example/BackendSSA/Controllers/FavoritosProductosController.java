package com.example.BackendSSA.Controllers;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoFavoritoProducto;
import com.example.BackendSSA.Dtos.DtoProducto;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.UserRepository;
import com.example.BackendSSA.Services.FavoritoProductoService;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritosProductosController {

    private final FavoritoProductoService favoritoProductoService;
    private final UserRepository userRepository;

    @Autowired
    public FavoritosProductosController(FavoritoProductoService favoritoProductoService, UserRepository userRepository) {
        this.favoritoProductoService = favoritoProductoService;
        this.userRepository = userRepository;
    }
    
    // Método auxiliar reutilizado de PerfilController para obtener el ID del usuario
    private Integer getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); 
        
        if (email == null || email.trim().isEmpty() || email.equals("anonymousUser")) {
             throw new RuntimeException("Token inválido o principal no encontrado.");
        }
        
        Usuario usuario = userRepository.findByemail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB."));
        return usuario.getIdUsuario();
    }

/* 
    /**
     * 💡 ENDPOINT 1: Obtener la lista de IDs de productos favoritos.
     * URL: GET /api/favoritos
     
    @GetMapping
    public ResponseEntity<?> obtenerIdsFavoritos() {
        try {
            Integer idUsuario = getAuthenticatedUserId();
            List<Integer> idsFavoritos = favoritoProductoService.obtenerIdsFavoritos(idUsuario);
            return new ResponseEntity<>(idsFavoritos, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); 
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al obtener favoritos.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/


    /**
     * 💡 ENDPOINT 2: Añadir o eliminar un producto de favoritos (TOGGLE).
     * URL: POST /api/favoritos
     */
    @PostMapping
    public ResponseEntity<?> toggleFavorito(@Validated @RequestBody DtoFavoritoProducto request) {
        try {
            Integer idUsuario = getAuthenticatedUserId();
            
            // Llama al servicio para añadir o eliminar el favorito
            boolean added = favoritoProductoService.toggleFavorito(idUsuario, request.getIdProducto());
            
            String message = added ? "Producto añadido a favoritos." : "Producto eliminado de favoritos.";
            
            // Devolvemos 200 OK con un mensaje indicando la acción
            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Maneja errores de Producto o Usuario no encontrado
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); 
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al gestionar favorito.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<DtoProducto>> getFavoritos() {
        try {
            Integer usuarioId = getAuthenticatedUserId();
            
            // Llama al nuevo método para obtener los detalles completos
            List<DtoProducto> favoritos = favoritoProductoService.getDetallesProductosFavoritos(usuarioId);
            
            return ResponseEntity.ok(favoritos);

        } catch (Exception e) {
            // Manejar errores de autenticación o de servidor
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
