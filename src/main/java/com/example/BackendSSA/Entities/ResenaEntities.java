package com.example.BackendSSA.Entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal; // Importación agregada para @Temporal
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resena")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idresena")
    private Integer idResena;

    // Se cambió 'Producto' por 'ProductoEntities' para coincidir con la convención
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    @JsonIgnore 
    private ProductoEntities producto;
    
    // Se cambió 'Usuario' por 'UsuarioEntities' para coincidir con la convención
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario; 
    
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaCreacion;
}