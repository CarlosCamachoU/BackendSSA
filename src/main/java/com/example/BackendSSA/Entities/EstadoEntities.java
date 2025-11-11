package com.example.BackendSSA.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "estado")
@AllArgsConstructor
@NoArgsConstructor
public class EstadoEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name=" idEstado")
    private Integer idEstado;

    @Column(name= "nombreestado", nullable = false, unique = true, length = 50)
    private String nombreEstado;

    @Column(name= "descripcionestado", columnDefinition = "VARCHAR(255)", nullable = true)
    private String descripcionEstado;


    
}
