package com.example.BackendSSA.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name ="resena")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResenaEntities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idresena")
    private Integer idResena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private ProductoEntities producto; 

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario; // ¡Referencia a tu clase Usuario!

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaCreacion; 

}
