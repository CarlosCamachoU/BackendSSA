package com.example.BackendSSA.Dtos;

import com.example.BackendSSA.Entities.Usuario;

import lombok.Data;

@Data
public class DtoAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";    
    private Usuario usuario;

    public DtoAuthResponse(String accessToken, Usuario usuario) {
        this.accessToken = accessToken;
        this.usuario = usuario;
    }

    public DtoAuthResponse(String accessToken){
        this.accessToken = accessToken;
    }  
    
}
