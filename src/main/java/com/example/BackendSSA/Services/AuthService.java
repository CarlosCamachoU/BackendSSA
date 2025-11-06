package com.example.BackendSSA.Services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.UserRepository;

@Service
public class AuthService implements IAuthService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IEmailService emailService;
    
    // --- LÓGICA DE SOLICITUD DE RESTABLECIMIENTO ---
    @Override
    public void requestPasswordReset(String email) {
        // 1. Buscar usuario por email
        Optional<Usuario> userOptional = userRepository.findByemail(email);

        // 2. Si el usuario existe, generamos el token
        if (userOptional.isPresent()) {
            Usuario user = userOptional.get();

            // 3. Generar token único y fecha de expiración (60 minutos)
            String token = UUID.randomUUID().toString();
            
            // Usamos LocalDateTime para la fecha de expiración
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(60);

            // 4. Guardar token y fecha de expiración en la base de datos
            user.setResetPasswordToken(token);
            user.setTokenExpiryDate(expiryTime); // Asignación directa de LocalDateTime
            userRepository.save(user);

            // 5. Enviar el email
            emailService.sendPasswordResetEmail(email, token);
        } 
        // Nota de seguridad: Si el usuario no existe, la función no hace nada y el controlador 
        // devuelve un mensaje genérico.
    }

    // --- LÓGICA DE RESTABLECIMIENTO FINAL ---
    @Override
    public void resetPassword(String token, String newPassword) throws Exception {
        
        // 1. Buscar usuario por el token
        Optional<Usuario> userOptional = userRepository.findByResetPasswordToken(token);

        if (!userOptional.isPresent()) {
            throw new Exception("El token proporcionado no es válido o ya fue utilizado.");
        }

        Usuario user = userOptional.get();
        LocalDateTime expiryDate = user.getTokenExpiryDate();

        // 2. Verificar si el token ha expirado
        // Comparamos la fecha de expiración con la hora actual
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
             // Limpiar token expirado
            user.setResetPasswordToken(null);
            user.setTokenExpiryDate(null);
            userRepository.save(user);
            throw new Exception("El enlace ha expirado. Por favor, solicita uno nuevo.");
        }
        
        // 3. Actualizar la contraseña
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setContrasenaHash(hashedNewPassword);

        // 4. Limpiar los campos de token de restablecimiento para invalidar el enlace usado
        user.setResetPasswordToken(null);
        user.setTokenExpiryDate(null);
        
        userRepository.save(user);
    }
}
