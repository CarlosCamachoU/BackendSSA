package com.example.BackendSSA.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BackendSSA.Entities.FavoritosEntities;
import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Entities.Usuario;

public interface FavoritoProductoRepository extends JpaRepository<FavoritosEntities, Integer> {
    Optional<FavoritosEntities> findByUsuarioAndProducto(Usuario usuario, ProductoEntities producto);

    // Método para obtener todos los favoritos de un usuario (para la vista "Favoritos")
    List<FavoritosEntities> findByUsuario(Usuario usuario);

    // Encuentra todos los IDs de producto que el usuario ha marcado como favoritos
   // List<Integer> findIdProductoByUsuarioId(Integer idusuario);
    
}
