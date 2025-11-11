package com.example.BackendSSA.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.BackendSSA.Dtos.DtoPreferencias;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.UserRepository;
import com.example.BackendSSA.Services.PreferenciasService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/preferencias")
public class PreferenciasController {

    private final PreferenciasService preferenciasService;
    private final UserRepository userRepository; // Necesario para obtener el idUsuario

    @Autowired
    public PreferenciasController(PreferenciasService preferenciasService, UserRepository userRepository) {
        this.preferenciasService = preferenciasService;
        this.userRepository = userRepository;
    }

    /**
     * Método auxiliar para obtener el ID del usuario autenticado.
     */
    private Integer getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); 
        
        Usuario usuario = userRepository.findByemail(email)
                                        .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB."));
        return usuario.getIdUsuario();
    }

    /**
     * Obtiene las preferencias de personalización del usuario autenticado.
     * URL: GET /api/preferencias
     */
    @GetMapping
    public ResponseEntity<?> obtenerPreferencias() {
        try {
            Integer idUsuario = getAuthenticatedUserId();
            DtoPreferencias dto = preferenciasService.obtenerPreferencias(idUsuario);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al obtener las preferencias.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza las preferencias de personalización del usuario autenticado.
     * URL: PUT /api/preferencias
     */
    @PutMapping
    public ResponseEntity<?> actualizarPreferencias(@RequestBody DtoPreferencias request) {
        try {
            Integer idUsuario = getAuthenticatedUserId();

             // Llama al servicio para actualizar los datos
            preferenciasService.actualizarPreferencias(idUsuario, request);
            
            // 💡 CAMBIO CRÍTICO: Devolvemos 204 No Content (Éxito sin cuerpo)
            return ResponseEntity.noContent().build(); 
            /* 
            DtoPreferencias dtoActualizado = preferenciasService.actualizarPreferencias(idUsuario, request);
            return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);*/

            
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno al actualizar las preferencias.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
}
