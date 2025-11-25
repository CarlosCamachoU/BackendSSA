package com.example.BackendSSA.Dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoPedidoRespuesta {
    private Integer idPedido;
    private Integer numeroPedidoCliente;

    private LocalDateTime fechaPedido;
    private BigDecimal total;
  
    private String direccionEnvio;
    private String dni;
    private String metodoPago;
    private String idTransaccion; // paymentIntentId (antes idTransaccionCulqi)

    private List<DtoDetallePedidoRespuesta> detalles;

    
}
