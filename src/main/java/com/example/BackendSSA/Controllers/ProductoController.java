package com.example.BackendSSA.Controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Repositories.ProductoRepository;

import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<ProductoEntities>> getAllProductos(
            @RequestParam(required = false) Integer idCategoria, // 🛑 Usamos Integer
            @RequestParam(required = false) BigDecimal precioMin, 
            @RequestParam(required = false) BigDecimal precioMax,
            /*Pageable pageable,*/
            // (La ordenación 'sortBy' la manejaremos en el frontend por ahora, o la podemos añadir aquí después)
            @RequestParam(required = false) String sortBy) { 

        Specification<ProductoEntities> spec = (root, query, criteriaBuilder) -> {
            Predicate finalPredicate = criteriaBuilder.conjunction();

            // 1. FILTRO POR CATEGORÍA
            if (idCategoria != null) {
                // Filtra por el campo idCategoria en la entidad Producto
                finalPredicate = criteriaBuilder.and(
                    finalPredicate, 
                    criteriaBuilder.equal(root.get("idCategoria"), idCategoria)
                );
            }

            // 2. FILTRO POR RANGO DE PRECIO
            if (precioMin != null) {
                 finalPredicate = criteriaBuilder.and(
                    finalPredicate, 
                    criteriaBuilder.greaterThanOrEqualTo(root.get("precioBase"), precioMin)
                );
            }
            if (precioMax != null) {
                 finalPredicate = criteriaBuilder.and(
                    finalPredicate, 
                    criteriaBuilder.lessThanOrEqualTo(root.get("precioBase"), precioMax)
                );
            }
            
            // 3. ORDENACIÓN (Si queremos hacerlo en el backend)
            if (sortBy != null && !sortBy.isEmpty()) {
                if ("precioAsc".equals(sortBy)) { // Ejemplo de parámetro
                    query.orderBy(criteriaBuilder.asc(root.get("precioBase")));
                } else if ("precioDesc".equals(sortBy)) {
                    query.orderBy(criteriaBuilder.desc(root.get("precioBase")));
                }
                // Si haces la ordenación aquí, quítala del código Vue
            }

            return finalPredicate;
        };

        // Ejecuta la consulta con los filtros dinámicos
         List<ProductoEntities> productos = productoRepository.findAll(spec);
        return ResponseEntity.ok(productos);

        // 🛑 Ejecuta la consulta, aplica filtros y paginación/ordenación
        //Page<ProductoEntities> productosPage = productoRepository.findAll(spec, pageable);
        
        // 🛑 EXTRAEMOS solo la lista de productos de la página para devolver List<ProductoEntities>
        //List<ProductoEntities> productosList = productosPage.getContent();

       // return ResponseEntity.ok(productosList);

    }



    /* 
    @GetMapping
    public ResponseEntity<List<ProductoEntities>> getAllProductos() {
        List<ProductoEntities> productos = productoRepository.findAll();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{idproducto}")
    public ResponseEntity<ProductoEntities> getProductoById(Long idproducto) {
        ProductoEntities producto = productoRepository.findById(idproducto).orElse(null);
        if (producto != null) {
            return ResponseEntity.ok(producto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    */

    
}
