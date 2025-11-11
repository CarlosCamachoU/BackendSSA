package com.example.BackendSSA.Dtos;

import java.math.BigDecimal;

import com.example.BackendSSA.Entities.ProductoEntities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoProducto {

    private Integer idproducto;
    private String nombre;
    private BigDecimal precioBase;
   // private String imagenUrl; // Asumo que este campo existe en ProductoEntities

    /**
     * Mapeador estático para convertir ProductoEntities a DtoProducto.
     * @param entity La entidad JPA ProductoEntities.
     * @return El DTO simple.
     */
    public static DtoProducto fromEntity(ProductoEntities entity) {
        return new DtoProducto(
            // Utilizamos el getter de la Entidad
            entity.getIdProducto(), 
            entity.getNombre(), 
            entity.getPrecioBase()
            // Esto asume que tienes un getter getImagenUrl() en ProductoEntities
            //entity.getImagenUrl() 
        );
    }
    
}
