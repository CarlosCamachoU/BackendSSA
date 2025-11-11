package com.example.BackendSSA.Dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class DtoPedido {

    //private Integer idUsuario;
    private String direccionEnvio;
    private BigDecimal total;
    private String culquitoken;
    private List<DtoDetallePedido> detalles;
    
}
