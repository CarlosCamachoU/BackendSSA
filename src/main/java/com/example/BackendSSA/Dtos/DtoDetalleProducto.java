package com.example.BackendSSA.Dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class DtoDetalleProducto {
    // --- 1. Detalles del Producto (Esenciales) ---
    private Integer idProducto;
    private String nombre;
    private String marca;
    private String sku;
    private BigDecimal precioBase; 
    private Integer stockActual;    
    private String categoria;
    private String descripcionCompleta;

    //private String urlImagen; // Campo asumido para el frontend


    // --- 3. Reseñas (Lista de DTOs) ---
    private List<DtoResena> resenas;

     // Constructor de mapeo (Usado en el Servicio para convertir la Entidad a DTO)
    public DtoDetalleProducto(
        Integer idProducto, String sku, String nombre, String descripcionCompleta, String marca, 
        String categoria, BigDecimal precioBase, Integer stockActual
    ) {
        this.idProducto = idProducto;
        this.sku = sku;
        this.nombre = nombre;
        this.descripcionCompleta = descripcionCompleta;
        this.marca = marca;
        // Asumiendo que la categoría se obtiene del objeto CategoriaEntities
        this.categoria = categoria; 
        this.precioBase = precioBase;
        this.stockActual = stockActual;
        //this.imagenPrincipalUrl = imagenPrincipalUrl;

    }
    
}
