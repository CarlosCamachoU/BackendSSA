package com.example.BackendSSA.Dtos;

//import java.math.BigDecimal;

import lombok.Data;

@Data
public class DtoDetallePedido {

    // Solo necesitamos el ID del producto, el resto de datos se obtienen del sistema
    private Integer idProducto;
    private Integer cantidad;
    // Se recomienda enviar el precio para validación, pero el Service debería buscar el precio actual
   // private BigDecimal precioUnitarioCompra; 
    
}
