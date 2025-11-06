package com.example.BackendSSA.Controllers;

import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoAuthResponse;
import com.example.BackendSSA.Dtos.DtoEmailRequest;
import com.example.BackendSSA.Dtos.DtoLogin;
import com.example.BackendSSA.Dtos.DtoRegistro;
import com.example.BackendSSA.Dtos.DtoResetPassword;
import com.example.BackendSSA.Entities.Rol;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.RolRepository;
import com.example.BackendSSA.Repositories.UserRepository;
import com.example.BackendSSA.Security.JwtGenerador;
import com.example.BackendSSA.Services.IAuthService;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin
public class authController {

    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private JwtGenerador jwtGenerador;
    private RolRepository rolRepository;
    private IAuthService authService;

    public authController(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
    UserRepository userRepository, IAuthService authService, RolRepository rolRepository, JwtGenerador jwtGenerador) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtGenerador = jwtGenerador;
        this.rolRepository = rolRepository;
        this.authService = authService;

    }
/* 
    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody DtoRegistro dtoRegistro){
        if(userRepository.existsByemail(dtoRegistro.getEmail())){
            return new ResponseEntity<>("El email ya está en uso", HttpStatus.BAD_REQUEST);
        } else{
            Usuario usuario = new Usuario();
            usuario.setNombres(dtoRegistro.getNombres()); 
            usuario.setApellidos(dtoRegistro.getApellidos());
            usuario.setEmail(dtoRegistro.getEmail());
            usuario.setContrasenaHash(passwordEncoder.encode(dtoRegistro.getContrasenaHash()));
            Rol rol = dtoRegistro.getNombreRol();
            usuario.setRol(rol);
            userRepository.save(usuario);
            return new ResponseEntity<>("Usuario registrado exitosamente", HttpStatus.OK);

    
        }
        
        
    }*/

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody DtoRegistro dtoRegistro){
        // 1. Verificar si el email ya está en uso
        if(userRepository.existsByemail(dtoRegistro.getEmail())){
            return new ResponseEntity<>("El email ya está en uso", HttpStatus.BAD_REQUEST);
        } 
        
        // 2. 🛑 Buscar el Rol por defecto ("Usuario" según tu DDL)
        Optional<Rol> rolOptional = rolRepository.findByNombreRol("Usuario");
        
        if (!rolOptional.isPresent()) {
            return new ResponseEntity<>("Error: Rol 'Usuario' no encontrado en la base de datos.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // 3. Crear y configurar el nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombres(dtoRegistro.getNombres()); 
        usuario.setApellidos(dtoRegistro.getApellidos());
        usuario.setEmail(dtoRegistro.getEmail());
        
        // 🛑 Corregido: Usar dtoRegistro.getPassword() o dtoRegistro.getContrasenaHash()
        // Asumiremos que el DTO envía la contraseña sin codificar en el campo `password`.
        usuario.setContrasenaHash(passwordEncoder.encode(dtoRegistro.getPassword()));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEsActivo(true); // Establecer el usuario como activo por defecto
        
        // 4. Asignar el rol y guardar
        usuario.setRol(rolOptional.get());
        userRepository.save(usuario);
        
        return new ResponseEntity<>("Usuario registrado exitosamente", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<DtoAuthResponse> login(@RequestBody DtoLogin dtoLogin){
        Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(dtoLogin.getEmail(), 
        dtoLogin.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerador.generarToken(authentication);
        Usuario usuario = userRepository.findByEmail(dtoLogin.getEmail());
        return new ResponseEntity<>(new DtoAuthResponse(token, usuario), HttpStatus.OK);    
    
    }

    //Endopoint para solicitar restablecimiento de contraseña
    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestBody DtoEmailRequest request) {
            
        authService.requestPasswordReset(request.getEmail());
        
        // Devolvemos una respuesta genérica para evitar la enumeración de usuarios.
        return ResponseEntity.ok("Si el correo está registrado, se ha iniciado el proceso de restablecimiento.");
    }
    
    // --- ENDPOINT PARA COMPLETAR EL RESTABLECIMIENTO DE CONTRASEÑA ---
    /**
     * Finaliza el proceso de restablecimiento. 
     * Recibe el token y la nueva contraseña para actualizar el registro del usuario.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody DtoResetPassword request) {
        
        try {
            // Llama al servicio para validar el token y actualizar la contraseña.
            authService.resetPassword(request.getToken(), request.getNewPassword());
            
            return ResponseEntity.ok("Contraseña actualizada exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.");
            
        } catch (Exception e) {
            // Captura excepciones de negocio (ej. token inválido, expirado, usuario no encontrado)
            return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(e.getMessage());
        }
    }



    
}
