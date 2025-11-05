package com.example.BackendSSA.Entities;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Producto")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ProductoEntities {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idProducto;

    // Relación con Categoría (asumiendo que tienes una entidad Categoria)
    @Column(name = "idcategoria") // 🛑 Mapea la columna idcategoria
    private Long idCategoria;
    @ManyToOne
    @JoinColumn(name = "idcategoria", nullable = false, insertable = false, updatable = false)
    private CategoriaEntities categoria;
    
    @Column(name= "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name= "descripcion", columnDefinition = "TEXT", nullable = true)
    private String descripcion;

    @Column(name = "preciobase", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioBase;

    
    @Column(name = "stock_actual", nullable = false) // 🛑 Mapea la columna stock_actual
    private Integer stockActual;

    @Column(name="marca", length = 100, nullable = true)
    private String marca;

    
}
