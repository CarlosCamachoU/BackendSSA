package com.example.BackendSSA.Dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoPerfil {

    // Campos requeridos en el registro inicial
    private String nombres;
    private String apellidos;
    private String email; 
    
    // Nuevos campos de Datos Personales
    private String telefono;
    
    // Se usa LocalDateTime, aunque en la DB sea DATE, para flexibilidad en la capa de Java.
    private LocalDateTime fechaNacimiento;

    // Campo de solo lectura para información de rol
    private String rol;
    
}
