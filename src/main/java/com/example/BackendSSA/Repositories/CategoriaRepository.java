package com.example.BackendSSA.Repositories;

//import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;

import com.example.BackendSSA.Entities.CategoriaEntities;

public interface CategoriaRepository extends JpaRepository<CategoriaEntities, Long> {
    
    /* 
     @Query("SELECT c FROM categoriaEntity c WHERE c.estadoCategoria = true")
    public List<CategoriaEntities> findAllTrue();

    //Hace busqueda por el nombre de la categoria y trae una categoria
    @Query("SELECT c FROM categoriaEntity c WHERE c.nombreCategoria = :categoria")
    public categoriaEntity traerPorCategoria(String categoria);

    @Query("SELECT c FROM categoriaEntity c WHERE c.idCategoria = :id")
    public categoriaEntity getPorId(int id);
*/
}
