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
@Table(name = "Pedido")
@AllArgsConstructor
@NoArgsConstructor
public class PedidoEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idpedido")
    private Integer idPedido;

    // Relación ManyToOne con UsuarioEntity (FK: idusuario)
    // Un pedido pertenece a un solo usuario.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario", nullable = false)
    private Usuario usuario; 
    
    // Relación ManyToOne con EstadoEntity (FK: idestado)
    // Indica el estado actual del pedido.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idestado", referencedColumnName = "idestado", nullable = false)
    @JsonBackReference
    private EstadoEntities estado; 

    @Column(name= "fechapedido", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name= "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name= "direccionenvio", columnDefinition = "TEXT", nullable = false)
    private String direccionEnvio;

    // NUEVO CAMPO PARA ALMACENAR EL TOKEN DE CULQI
    @Column(name = "culqi_token", length = 255)
    private String culqiToken;

    // Relación OneToMany con DetallePedidoEntity
    // Mapea la lista de productos de este pedido.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetallePedidoEntities> detalles;






    
}
