package com.example.BackendSSA.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "pedido")
@AllArgsConstructor
@NoArgsConstructor
public class PedidoEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idpedido")
    private Integer idPedido;

    // Relación ManyToOne con Usuario (FK: idusuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario", nullable = false)
    private Usuario usuario; 
    
    // Relación ManyToOne con EstadoEntities (FK: idestado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idestado", referencedColumnName = "idestado", nullable = false)
    @JsonBackReference
    private EstadoEntities estado; 

    @Column(name = "fechapedido", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    // Dirección de envío congelada al momento del pedido
    @Column(name = "direccionenvio", columnDefinition = "TEXT", nullable = false)
    private String direccionEnvio;

    // DNI usado en este pedido (no necesariamente el mismo que quede luego en el perfil)
    @Column(name = "dni", length = 15)
    private String dni;

    // Método de pago (ej: TARJETA_CULQI, EFECTIVO, YAPE, etc.)
    @Column(name = "metodopago", length = 30, nullable = false)
    private String metodoPago;

    // ID de la transacción en Culqi (NO es el token del front, es el chargeId)
    @Column(name = "idtransaccionculqi", length = 255)
    private String idTransaccionCulqi;

    @Column(name = "numeroPedidoCliente")
    private Integer numeroPedidoCliente;


    // Relación OneToMany con DetallePedidoEntities
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetallePedidoEntities> detalles;

}
