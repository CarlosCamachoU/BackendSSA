package com.example.BackendSSA.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.BackendSSA.Entities.ProductoEntities;

public interface ProductoRepository extends JpaRepository<ProductoEntities, Long>, JpaSpecificationExecutor<ProductoEntities> {
    
}
