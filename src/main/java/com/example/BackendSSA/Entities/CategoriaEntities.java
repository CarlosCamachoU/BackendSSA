package com.example.BackendSSA.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "Categoria")
@AllArgsConstructor
@NoArgsConstructor

public class CategoriaEntities {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategoria")
    private Integer idcategoria;

    @Column(name= "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name= "descripcion", columnDefinition = "TEXT", nullable = true)
    private String descripcion;

}
