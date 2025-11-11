package com.example.BackendSSA.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.BackendSSA.Entities.ProductoEntities;

public interface ProductoRepository extends JpaRepository<ProductoEntities, Integer>, JpaSpecificationExecutor<ProductoEntities> {
    
     /**
     * Recupera una lista de productos ordenada por un score de personalización basado
     * en las preferencias del usuario autenticado.
     * * @param color El color favorito del usuario (asumido: un solo valor o el primero de la lista).
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
        // Se suman puntos por cada coincidencia de preferencia
        // ----------------------------------------------------
        
        // 1. Coincidencia de Estilo (Alto valor: 15 puntos)
        " (CASE WHEN p.estilo = :estilo THEN 15 ELSE 0 END) + " +
        
        // 2. Coincidencia de Color (Medio valor: 10 puntos)
        " (CASE WHEN p.color = :color THEN 10 ELSE 0 END) + " +

        // 3. Coincidencia de Intereses (Multi-valor, 7 puntos por coincidencia)
        // Usamos FIND_IN_SET ya que el campo 'interes' del producto contiene multiples valores separados por coma.
        // Asumimos que la tabla de Preferencias del usuario ya está normalizada en el Servicio para un solo valor de interes a la vez.
        " (CASE WHEN FIND_IN_SET(:interes, p.interes) > 0 THEN 7 ELSE 0 END) + " +
        
        // 4. Coincidencia de Hobbie (Multi-valor, 5 puntos por coincidencia)
        " (CASE WHEN FIND_IN_SET(:hobbie, p.hobbie) > 0 THEN 5 ELSE 0 END) + " +
        
        // 5. Coincidencia de Profesión (Multi-valor, 5 puntos por coincidencia. Asumimos que :profesion es un solo valor)
        // También verifica si el campo de profesión del producto tiene el tag 'Todos'
        " (CASE WHEN FIND_IN_SET(:profesion, p.profesion) > 0 THEN 5 " + 
        "       WHEN FIND_IN_SET('Todos', p.profesion) > 0 THEN 2 ELSE 0 END) AS score_personalizado " +
        
        // ----------------------------------------------------
        // FILTRADO ESTRICTO (Si aplica)
        // Solo para productos que coincidan estrictamente con un filtro (ej: Talla)
        // ----------------------------------------------------
        "FROM Producto p " +
        // Filtro estricto: el producto debe tener la talla preferida del usuario en su campo 'talla'.
        "WHERE FIND_IN_SET(:talla, p.talla) > 0 OR p.talla IS NULL " + // Permite productos sin talla o que coincidan.
        
        // ----------------------------------------------------
        // ORDENAMIENTO FINAL
        // ----------------------------------------------------
        "ORDER BY score_personalizado DESC, p.stock_actual DESC", // Primero por score, luego por stock/popularidad
        nativeQuery = true)
    List<ProductoEntities> findPersonalizedProducts(
            @Param("color") String color, 
            @Param("estilo") String estilo,
            @Param("talla") String talla,
            @Param("interes") String interes,
            @Param("hobbie") String hobbie,
            @Param("profesion") String profesion);
}
