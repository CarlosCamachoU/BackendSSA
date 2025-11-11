package com.example.BackendSSA.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoPreferencias {

     // Campos de Preferencias (en formato CSV String)
    private String hobbies;
    private String coloresFavoritos;
    private String intereses;
    private String tallas;
    private String profesion;
    private String estilos;
    
}
