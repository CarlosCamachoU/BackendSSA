package com.example.BackendSSA.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BackendSSA.Entities.DetallePedidoEntities;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedidoEntities, Integer>  {
    
    
}
