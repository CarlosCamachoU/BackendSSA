package com.example.BackendSSA.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BackendSSA.Entities.EstadoEntities;

@Repository
public interface EstadoRepository extends JpaRepository<EstadoEntities, Integer>{

    // Método útil para buscar el estado inicial por nombre (ej: "PENDIENTE")
    Optional<EstadoEntities> findByNombreEstado(String nombreEstado);
    

}
