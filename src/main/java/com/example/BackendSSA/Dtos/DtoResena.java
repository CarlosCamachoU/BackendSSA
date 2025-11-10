package com.example.BackendSSA.Dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoResena {
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fecha;
    private String nombreUsuario; // Solo el nombre, no el ID o email del usuario
    
}
