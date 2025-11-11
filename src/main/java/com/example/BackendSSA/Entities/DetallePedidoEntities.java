package com.example.BackendSSA.Entities;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "DetallePedido")
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedidoEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle")
    private Integer iddetalle;

    // Relación ManyToOne con PedidoEntity (FK: idpedido)
    // Un detalle pertenece a un solo pedido.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpedido", referencedColumnName = "idpedido", nullable = false)
    @JsonBackReference
    private PedidoEntities pedido;

    // Relación ManyToOne con ProductoEntity (FK: idproducto)
    // Un detalle corresponde a un solo producto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", referencedColumnName = "idproducto", nullable = false)
    private ProductoEntities producto; 

    @Column(name= "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name= "preciounitariocompra", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioCompra; 






    
}
