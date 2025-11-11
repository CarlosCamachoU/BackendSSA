package com.example.BackendSSA.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BackendSSA.Entities.ResenaEntities;

@Repository
public interface ResenaRepository extends JpaRepository<ResenaEntities, Integer>{ 
    
    List<ResenaEntities> findByProductoIdProductoOrderByFechaCreacionDesc(Integer idProducto);
    
}
