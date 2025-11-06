package com.example.BackendSSA.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String FRONTEND_BASE_URL = "http://localhost:8081"; // Cambia esto según la URL de tu frontend
    
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();

        String resetUrl = FRONTEND_BASE_URL + "/reset-password?token=" + resetToken;

        message.setFrom("sagasmartconfiguracion@gmail.com"); // Cambia esto por tu correo SMTP
        message.setTo(toEmail);
        message.setSubject("Restablecimiento de Contraseña");
        
        String emailBody = "Hola,\n\n"
                + "Hemos recibido una solicitud para restablecer tu contraseña. "
                + "Por favor, haz clic en el siguiente enlace para restablecer tu contraseña:\n\n"
                + resetUrl + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este correo electrónico.\n\n"
                + "Saludos,\n"
                + "El equipo de SagaSmart";

                message.setText(emailBody);

                mailSender.send(message);
    }
    
}
