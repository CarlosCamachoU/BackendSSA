package com.example.BackendSSA.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.BackendSSA.Entities.Rol;

@Repository 
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombreRol(String nombreRol);

    @Query("SELECT r FROM Rol r WHERE r.nombreRol = :nombreRol")
    Rol findByRol(String nombreRol);

    
}
