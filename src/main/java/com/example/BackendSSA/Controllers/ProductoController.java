package com.example.BackendSSA.Controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoDetalleProducto;
import com.example.BackendSSA.Dtos.DtoProducto;
import com.example.BackendSSA.Dtos.DtoResena;
import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.example.BackendSSA.Repositories.UserRepository;
import com.example.BackendSSA.Services.ProductoService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final UserRepository userRepository;

   @Autowired
    public ProductoController(ProductoRepository productoRepository, ProductoService productoService, UserRepository userRepository) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.userRepository = userRepository;
    }


    /**
     * Obtiene los productos personalizados para el usuario logueado.
     * La lista está ordenada por score de relevancia.
     */
    @GetMapping("/personalizados")
    public ResponseEntity<List<DtoProducto>> getPersonalizedProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            // Esto no debería pasar si el endpoint está bien configurado con Spring Security
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1. Obtener el ID del usuario
            String email = userDetails.getUsername();
            Usuario usuario = userRepository.findByemail(email) // Asumo findByEmail existe y es tu método de búsqueda
                    .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado con email: " + email));
            
            Integer userId = usuario.getIdUsuario(); // Tu campo es Integer
            
            // 2. Llamar al servicio para obtener la lista personalizada
            List<DtoProducto> personalizedProducts = productoService.getProductosPersonalizados(userId);
            
            return ResponseEntity.ok(personalizedProducts);

        } catch (EntityNotFoundException e) {
            // Si el usuario no existe (o sus preferencias, manejado en el service)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
/* 
    @GetMapping
    public ResponseEntity<List<ProductoEntities>> getAllProductos(
            @RequestParam(required = false) Integer idCategoria, // 🛑 Usamos Integer
            @RequestParam(required = false) BigDecimal precioMin, 
            @RequestParam(required = false) BigDecimal precioMax,

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


    }*/

    @GetMapping
    public ResponseEntity<List<DtoProducto>> getAllProductos( // <-- CAMBIO CLAVE AQUÍ: DtoProducto
            @RequestParam(required = false) Integer idCategoria, 
            @RequestParam(required = false) BigDecimal precioMin, 
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String sortBy) { 

        Specification<ProductoEntities> spec = (root, query, criteriaBuilder) -> {
            Predicate finalPredicate = criteriaBuilder.conjunction();
            
            // ... (Tu lógica de filtrado por categoría, precio y ordenación se mantiene) ...

            // 1. FILTRO POR CATEGORÍA
            if (idCategoria != null) {
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
            // 3. ORDENACIÓN
            if (sortBy != null && !sortBy.isEmpty()) {
                if ("precioAsc".equals(sortBy)) { 
                    query.orderBy(criteriaBuilder.asc(root.get("precioBase")));
                } else if ("precioDesc".equals(sortBy)) {
                    query.orderBy(criteriaBuilder.desc(root.get("precioBase")));
                }
            }

            return finalPredicate;
        };

        // 1. Ejecuta la consulta y obtiene las Entidades
        List<ProductoEntities> entities = productoRepository.findAll(spec);
        
        // 2. CONVERSIÓN DE ENTIDAD A DTO
        List<DtoProducto> dtos = entities.stream()
                .map(DtoProducto::fromEntity) // Usamos el mapper estático
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos); // <-- Retorna la lista de DTOs
    }
    



    @GetMapping("/{id}")
    public ResponseEntity<?> getProductoDetalle(@PathVariable Integer id) {
        try {
            DtoDetalleProducto detalle = productoService.getProductoDetalle(id);
            return ResponseEntity.ok(detalle);
        } catch (EntityNotFoundException e) {
            // Retorna 404 Not Found si el producto no existe
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Retorna 500 Internal Server Error para otros problemas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al obtener el detalle del producto: " + e.getMessage());
        }
    }
/* 
    @PostMapping("/resena") // Nuevo endpoint para publicar reseñas
    public ResponseEntity<DtoResena> publicarResena(@RequestBody DtoResena resena) {
        try {
            // Lógica para guardar la reseña
            DtoResena nuevaResena = productoService.guardarResena(resena);
            
            // Retorna la reseña recién guardada con su ID y fecha asignados
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaResena);
        } catch (Exception e) {
            // Manejo de errores (ej. producto no encontrado, campos nulos)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }*/


    @PostMapping("/{idProducto}/resena") 
    public ResponseEntity<?> publicarResena(
        @PathVariable Integer idProducto, 
        @RequestBody DtoResena resena) {
            try {
                // Ahora pasamos AMBOS argumentos al servicio
                DtoResena nuevaResena = productoService.guardarResena(idProducto, resena);
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevaResena);
            } catch (EntityNotFoundException e) {
                // Retorna 404 si el producto con ese ID no existe
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado para la reseña.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

  /**
     * Endpoint GET /api/productos/{idProducto}/resena
     * Obtiene solo la lista de reseñas para un producto específico.
     */
    @GetMapping("/{idProducto}/resena")
    public ResponseEntity<?> getResenasByProducto(@PathVariable Integer idProducto) {
        try {
            // LLAMA AL NUEVO MÉTODO DEL SERVICE
            List<DtoResena> resenas = productoService.getResenasByProductoId(idProducto);
            
            // Retorna 200 OK con la lista (vacía o no)
            return ResponseEntity.ok(resenas);

        } catch (EntityNotFoundException e) {
            // Retorna 404 Not Found si el producto base no existe.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Retorna 500 Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al obtener las reseñas: " + e.getMessage());
        }
    }


    @GetMapping("/ofertas")
    public ResponseEntity<List<DtoProducto>> getProductosEnOferta() {
        try {
            List<ProductoEntities> entities = productoRepository.findProductosEnOferta();
            
            List<DtoProducto> dtos = entities.stream()
                .map(DtoProducto::fromEntity)
                .collect(Collectors.toList());
            
        
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    
    }


    @GetMapping("/buscar")
    public ResponseEntity<List<DtoProducto>> buscarProductos(
        @RequestParam("query") String query,
        @RequestParam(value = "limit", defaultValue = "5") int limit) {
            
            try {
            // Página 0, tamaño = limit (por defecto 5 productos máximo)
                Pageable pageable = PageRequest.of(0, limit);

            // Usamos tu método ya existente en el repositorio
            List<ProductoEntities> entities = productoRepository.buscarPorCriterioEnCampos(query, pageable);

            // Mapear a DTO
             List<DtoProducto> dtos = entities.stream()
             .map(DtoProducto::fromEntity)
             .collect(Collectors.toList());

             return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
  
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