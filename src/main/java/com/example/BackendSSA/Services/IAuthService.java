package com.example.BackendSSA.Services;

public interface IAuthService {
    
    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword) throws Exception;

    
}
