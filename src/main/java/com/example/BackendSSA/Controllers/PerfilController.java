package com.example.BackendSSA.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.example.BackendSSA.Dtos.DtoPerfil;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.UserRepository;
import com.example.BackendSSA.Services.PerfilService;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final PerfilService perfilService;
    private final UserRepository userRepository; // Necesario para obtener el idUsuario

    @Autowired
    public PerfilController(PerfilService perfilService, UserRepository userRepository) {
        this.perfilService = perfilService;
        this.userRepository = userRepository;
    }

    /**
     * Método auxiliar para obtener el ID del usuario autenticado.
     */
    private Integer getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // El getName() generalmente devuelve el 'principal', que en JWT es el email (username)
        String email = authentication.getName(); 
        
        Usuario usuario = userRepository.findByemail(email)
                                        .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB."));
        return usuario.getIdUsuario();
    }

    /**
     * Obtiene los datos del perfil del usuario autenticado.
     * URL: GET /api/perfil
     */
    @GetMapping
    public ResponseEntity<?> obtenerPerfil() {
        try {
            Integer idUsuario = getAuthenticatedUserId();
            DtoPerfil perfil = perfilService.obtenerPerfil(idUsuario);
            return new ResponseEntity<>(perfil, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Maneja el error de usuario no encontrado o DB
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); 
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al obtener el perfil.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza los datos del perfil del usuario autenticado.
     * URL: PUT /api/perfil
     */
    @PutMapping
    public ResponseEntity<?> actualizarPerfil(@RequestBody DtoPerfil request) {
        try {
            Integer idUsuario = getAuthenticatedUserId();

            // Llama al servicio para actualizar los datos
            perfilService.actualizarPerfil(idUsuario, request);
            
            // 💡 CAMBIO CRÍTICO: Devolvemos 204 No Content (Éxito sin cuerpo)
            return ResponseEntity.noContent().build();
            /* 
            DtoPerfil perfilActualizado = perfilService.actualizarPerfil(idUsuario, request);
            return new ResponseEntity<>(perfilActualizado, HttpStatus.OK);*/
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al actualizar el perfil.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
