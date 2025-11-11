package com.example.BackendSSA.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BackendSSA.Entities.PedidoEntities;

public interface PedidoRepository extends JpaRepository<PedidoEntities, Integer> {
    
}
