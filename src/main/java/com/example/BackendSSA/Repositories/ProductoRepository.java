package com.example.BackendSSA.Repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.BackendSSA.Entities.ProductoEntities;

public interface ProductoRepository extends JpaRepository<ProductoEntities, Integer>, JpaSpecificationExecutor<ProductoEntities> {

    /* 
    List<ProductoEntities> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrCategoriaContainingIgnoreCase(
        String nombre,
        String descripcion,
        String idcategoria,
        Pageable pageable
    );
    */
    // 🏆 Búsqueda por nombre y descripción (sin usar lower sobre categoria)
    @Query("SELECT p FROM ProductoEntities p " +
           "WHERE lower(p.nombre) LIKE lower(concat('%', :query, '%')) " +
           "OR lower(p.descripcion) LIKE lower(concat('%', :query, '%')) " +
           "ORDER BY p.precioBase DESC") // O el orden que prefieras para relevancia
    List<ProductoEntities> buscarPorCriterioEnCampos(
        @Param("query") String query,
        Pageable pageable
    );


    /**
     * Productos en oferta (en_oferta = true y stock > 0).
     * Usamos COALESCE para ordenar por precio_oferta si existe,
     * y si es null, por preciobase.
     */
    @Query("SELECT p FROM ProductoEntities p " +
           "WHERE p.enOferta = true " +
           "  AND p.stockActual > 0 " +
           "ORDER BY COALESCE(p.precioOferta, p.precioBase) ASC")
    List<ProductoEntities> findProductosEnOferta();

    /**
     * Recupera una lista de productos ordenada por un score de personalización basado
     * en las preferencias del usuario autenticado.
     * @param color El color favorito del usuario (asumido: un solo valor o el primero de la lista).
     * @param estilo El estilo de preferencia del usuario (asumido: un solo valor o el primero de la lista).
     * @param talla La talla preferida del usuario para filtrado estricto (si aplica, asumido: un solo valor).
     * @param interes Los intereses del usuario (lista de valores separados por coma).
     * @param hobbie Los hobbies del usuario (lista de valores separados por coma).
     * @param profesion La profesión del usuario (un solo valor).
     * @return Una lista de ProductoEntities, ordenada por Score descendente.
     */
    @Query(value = 
        "SELECT p.*, " +
        // ----------------------------------------------------
        // CALCULO DE PUNTAJE (SCORE)
        // ----------------------------------------------------
        " (CASE WHEN p.estilo = :estilo THEN 15 ELSE 0 END) + " +    // Estilo
        " (CASE WHEN p.color = :color THEN 10 ELSE 0 END) + " +      // Color
        " (CASE WHEN FIND_IN_SET(:interes, p.interes) > 0 THEN 7 ELSE 0 END) + " +  // Interes
        " (CASE WHEN FIND_IN_SET(:hobbie, p.hobbie) > 0 THEN 5 ELSE 0 END) + " +    // Hobbie
        " (CASE WHEN FIND_IN_SET(:profesion, p.profesion) > 0 THEN 5 " +            // Profesión
        "       WHEN FIND_IN_SET('Todos', p.profesion) > 0 THEN 2 ELSE 0 END) AS score_personalizado " +
        // ----------------------------------------------------
        // FILTRADO ESTRICTO (Talla)
        // ----------------------------------------------------
        "FROM Producto p " +
        "WHERE FIND_IN_SET(:talla, p.talla) > 0 OR p.talla IS NULL " +
        // ----------------------------------------------------
        // ORDENAMIENTO FINAL
        // ----------------------------------------------------
        "ORDER BY score_personalizado DESC, p.stock_actual DESC",
        nativeQuery = true)
    List<ProductoEntities> findPersonalizedProducts(
            @Param("color") String color, 
            @Param("estilo") String estilo,
            @Param("talla") String talla,
            @Param("interes") String interes,
            @Param("hobbie") String hobbie,
            @Param("profesion") String profesion);
}
