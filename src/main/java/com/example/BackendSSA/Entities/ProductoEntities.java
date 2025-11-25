package com.example.BackendSSA.Entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ProductoEntities {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idProducto;

    // Relación con Categoría 
    @Column(name = "idcategoria") 
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

    @Column(name = "stock_actual", nullable = false) 
    private Integer stockActual;

    @Column(name="marca", length = 100, nullable = true)
    private String marca;

    @Column(name = "sku", length = 50, unique = true)
    private String sku;

    // Color principal del producto
    @Column(name = "color", length = 100, nullable = true)
    private String color;

    // Estilo del producto (e.g., Minimalista, Casual, Elegante)
    @Column(name = "estilo", length = 100, nullable = true)
    private String estilo;

    // Talla o conjunto de tallas disponibles (e.g., M, L, XL; o 32, 34)
    @Column(name = "talla", length = 50, nullable = true)
    private String talla;

    // Intereses o Tags asociados al producto (valores separados por coma)
    @Column(name = "interes", length = 255, nullable = true)
    private String interes;

    // Hobbies asociados al producto (valores separados por coma)
    @Column(name = "hobbie", length = 255, nullable = true)
    private String hobbie;

    // Profesiones relevantes para el producto (valores separados por coma)
    @Column(name = "profesion", length = 255, nullable = true)
    private String profesion;

    @Column(name = "enOferta")
    private Boolean enOferta;

    @Column(name = "precioOferta")
    private BigDecimal precioOferta;

    @Column(name = "fechaInicioOferta")
    private LocalDate fechaInicioOferta;

    @Column(name = "fechaFinOferta")
    private LocalDate fechaFinOferta;

    //crear una ruta para la imagen.
    // Si necesitas una columna para la URL de la imagen:
    @Column(name = "imagenUrl", length = 500, nullable = true)
    private String imagenUrl;    
    
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<ResenaEntities> resenas;
    
    @JsonIgnore
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoritosEntities> usuariosFavorito; // Puedes nombrar esta lista como prefieras



    
}
