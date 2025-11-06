package com.example.BackendSSA.Services;




public interface IEmailService {
    

    void sendPasswordResetEmail(String toEmail, String resetToken);
}
