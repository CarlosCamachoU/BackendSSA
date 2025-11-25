package com.example.BackendSSA.Controllers;

import java.util.HashMap;
import java.util.Map;

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
     
    private Integer getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // El getName() generalmente devuelve el 'principal', que en JWT es el email (username)
        String email = authentication.getName(); 
        
        Usuario usuario = userRepository.findByemail(email)
                                        .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB."));
        return usuario.getIdUsuario();
    }*/

        

    private Usuario getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); 
        
        // Busca y devuelve la entidad de usuario
        return userRepository.findByemail(email)
                             .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en DB."));
    }


    // --- NUEVO ENDPOINT ---
    /**
     * Devuelve el estado de la bandera de personalización del perfil del usuario autenticado.
     * URL: GET /api/perfil/estado
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Boolean>> obtenerEstadoPerfil() {
        try {
            Usuario usuario = getAuthenticatedUser();
            
            // Creamos un mapa para devolver la bandera en formato JSON
            Map<String, Boolean> estado = new HashMap<>();
            // El nombre de la clave "perfilCompleto" debe coincidir con el esperado en el frontend
            estado.put("perfilCompleto", usuario.isPerfilCompleto()); 
            
            return new ResponseEntity<>(estado, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Manejo de error si el usuario no es encontrado (debería ser raro si está autenticado)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    /**
     * Obtiene los datos del perfil del usuario autenticado.
     * URL: GET /api/perfil
     */
    @GetMapping
    public ResponseEntity<?> obtenerPerfil() {
        try {
            Integer idUsuario = getAuthenticatedUser().getIdUsuario();
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
            //Integer idUsuario = getAuthenticatedUserId();

            Usuario usuario = getAuthenticatedUser();
            Integer idUsuario = usuario.getIdUsuario();


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
