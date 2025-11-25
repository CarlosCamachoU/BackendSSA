package com.example.BackendSSA.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BackendSSA.Entities.PedidoEntities;

public interface PedidoRepository extends JpaRepository<PedidoEntities, Integer> {
    
 // ✅ Listar pedidos de un usuario (ya lo estabas usando)
    List<PedidoEntities> findByUsuario_IdUsuarioOrderByFechaPedidoDesc(Integer idUsuario);

    // ✅ Buscar un pedido específico del usuario actual
    Optional<PedidoEntities> findByIdPedidoAndUsuario_IdUsuario(Integer idPedido, Integer idUsuario);

    long countByUsuario_IdUsuario(Integer idUsuario);

}

