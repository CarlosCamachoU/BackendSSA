package com.example.BackendSSA.Dtos;

public class DtoResetPassword {
    private String token;
    private String newPassword;

    public DtoResetPassword() {
    }

    public DtoResetPassword(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
