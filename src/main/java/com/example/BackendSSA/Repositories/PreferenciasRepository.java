package com.example.BackendSSA.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BackendSSA.Entities.PreferenciasEntities;

public interface PreferenciasRepository extends JpaRepository<PreferenciasEntities, Integer> {
    
    Optional<PreferenciasEntities> findByUsuarioIdUsuario(Integer userId);
    
}
