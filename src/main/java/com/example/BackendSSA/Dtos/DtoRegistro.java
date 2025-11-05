package com.example.BackendSSA.Dtos;

import java.time.LocalDateTime;

import com.example.BackendSSA.Entities.Rol;

import lombok.Data;

@Data
public class DtoRegistro {
    // Se mapea a la columna 'nombres' de la tabla USUARIO
    private String nombres; 
    
    // Se mapea a la columna 'apellidos' de la tabla USUARIO
    private String apellidos;
    
    // Se mapea a la columna 'email' y es el campo de login
    private String email; 
    
    // La contraseña en texto plano, que se hashea en el AuthController
    private String password;

    private LocalDateTime fechaRegistro; 

    private Boolean esActivo;

    Rol nombreRol;



}
