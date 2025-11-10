package com.example.BackendSSA.Dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoDetalleProducto {

    private Integer idProducto;
    private String sku;
    private String nombre;
    private String descripcion;
    private String marca;
    private String categoria;
    private BigDecimal precioBase;
    private Integer stockActual;
    
    // Calificación
    private BigDecimal calificacionPromedio;
    private Integer numeroResenas;

    // Imágenes
    private String imagenPrincipalUrl;


    // Atributos Dinámicos (JSON)
    // El 'Map' permite que el JSON se deserialice directamente a un objeto clave-valor en Java
    private Map<String, Object> atributos; 
    
    
    // Reseñas (Usamos un DTO anidado para simplificar la información de la reseña)
    private List<DtoResena> resenas;
    
    // Constructor de mapeo (Usado en el Servicio para convertir la Entidad a DTO)
    public DtoDetalleProducto(
        Integer idProducto, String sku, String nombre, String descripcion, String marca, 
        String categoria, BigDecimal precioBase, Integer stockActual, String imagenPrincipalUrl, Map<String, Object> atributos
    ) {
        this.idProducto = idProducto;
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.marca = marca;
        // Asumiendo que la categoría se obtiene del objeto CategoriaEntities
        this.categoria = categoria; 
        this.precioBase = precioBase;
        this.stockActual = stockActual;
        this.imagenPrincipalUrl = imagenPrincipalUrl;
        this.atributos = atributos;
    }
    
}
