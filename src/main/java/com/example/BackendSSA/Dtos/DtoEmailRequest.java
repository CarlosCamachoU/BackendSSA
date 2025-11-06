package com.example.BackendSSA.Dtos;

public class DtoEmailRequest {
    private String email;

    public DtoEmailRequest() {
    }

    public DtoEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
