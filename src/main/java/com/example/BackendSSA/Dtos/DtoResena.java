package com.example.BackendSSA.Dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoResena {

    private Integer idResena;
    @JsonProperty("nombreUsuario")
    private String nombreUsuario; 
    private String comentario;
    private LocalDateTime fecha;

    public DtoResena(String comentario){
        this.comentario = comentario;
    }
    
}


