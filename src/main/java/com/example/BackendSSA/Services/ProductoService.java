package com.example.BackendSSA.Services; 

import com.example.BackendSSA.Dtos.DtoDetalleProducto;
//import com.example.BackendSSA.Dtos.DtoPreferencias;
import com.example.BackendSSA.Dtos.DtoResena;
import com.example.BackendSSA.Entities.ProductoEntities; 
import com.example.BackendSSA.Entities.ResenaEntities; 
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.example.BackendSSA.Repositories.ResenaRepository;
import com.example.BackendSSA.Repositories.UserRepository;
import org.springframework.security.core.Authentication;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ResenaRepository resenaRepository;
    private final UserRepository userRepository;
    //private final PreferenciasService preferenciasService;

    
    // Inyección de dependencias por constructor
    @Autowired
    public ProductoService(ProductoRepository productoRepository, ResenaRepository resenaRepository, UserRepository userRepository /*PreferenciasService preferenciasService*/) {
        this.productoRepository = productoRepository;
        this.resenaRepository = resenaRepository;
        this.userRepository = userRepository;
       // this.preferenciasService = preferenciasService;
    }/* 

     /**
     * Obtiene productos ordenados por relevancia de personalización 
     * basada en las preferencias del usuario logueado.
     * @return Lista de productos ordenada por score de personalización.
     * @throws EntityNotFoundException Si el usuario logueado no se encuentra.
     
    @Transactional(readOnly = true)
    public List<ProductoEntities> getProductosPersonalizados() {
        
        // 1. OBTENER ID DEL USUARIO LOGUEADO
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); 

        // Buscar la entidad Usuario completa por el email
        Usuario usuario = userRepository.findByemail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado con email: " + email));
        
        Integer userId = usuario.getIdUsuario(); // mi campo es getIdUsuario() y es Integer
        

        // 2. OBTENER LAS PREFERENCIAS DEL USUARIO
        // ----------------------------------------
        DtoPreferencias preferencias = preferenciasService.getPreferenciasByUserId(userId);

        if (preferencias == null) {
            // Si no hay preferencias, devuelve los productos por defecto (ej: todos, o un filtro base)
            return productoRepository.findAll();
        }

        // 3. EXTRAER Y NORMALIZAR LOS VALORES PARA LA CONSULTA NATIVA
        // -------------------------------------------------------------
        
        // Tomamos el primer valor para filtros únicos
        String colorPrincipal = getFirstValue(preferencias.getColoresFavoritos());
        String estiloPrincipal = getFirstValue(preferencias.getEstilos());
        String tallaPrincipal = getFirstValue(preferencias.getTallas()); 
        String profesion = getFirstValue(preferencias.getProfesion()); 

        // Unimos la lista de tags en una cadena separada por comas para FIND_IN_SET
        String tagsIntereses = joinTags(preferencias.getIntereses());
        String tagsHobbies = joinTags(preferencias.getHobbies());


        // 4. LLAMAR AL REPOSITORIO CON LOS PARÁMETROS OPTIMIZADOS
        // --------------------------------------------------------
        return productoRepository.findPersonalizedProducts(
                colorPrincipal,
                estiloPrincipal,
                tallaPrincipal,
                tagsIntereses,
                tagsHobbies,
                profesion
        );
    }
    
    /**
     * Extrae el primer valor de una lista para ser usado en el Repositorio.
     * @param values Lista de strings.
     * @return El primer valor o cadena vacía si la lista es nula o vacía.
     
    private String getFirstValue(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.get(0);
    }

    /**
     * Une una lista de strings en una sola cadena separada por comas (sin espacios).
     * Esto es crucial para la función FIND_IN_SET de MySQL.
     * @param values Lista de strings (ej: ["Tecnologia", "Gadgets"]).
     * @return Cadena unida (ej: "Tecnologia,Gadgets").
     
    private String joinTags(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream().collect(Collectors.joining(","));
    }*/





    /**
     * Busca un producto por ID y lo convierte a un DTO detallado, 
     * incluyendo la lista de reseñas.
     * @param id El ID del producto.
     * @return DtoDetalleProducto con toda la información.
     * @throws EntityNotFoundException si el producto no existe.
     */
    @Transactional(readOnly = true)
    public DtoDetalleProducto getProductoDetalle(Integer id) {
        // Usa findById().orElseThrow para obtener la entidad o lanzar una excepción
        ProductoEntities producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        
        // 2. Mapear las Reseñas a DTOs
       List<DtoResena> DTOresena = producto.getResenas().stream()
                .map(this::mapToResenaDTO)
                .collect(Collectors.toList());

        // 5. Construir el DTO principal
        DtoDetalleProducto dto = new DtoDetalleProducto(
                producto.getIdProducto(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getMarca(),
                // Asumiendo que la Entidad Categoria tiene un método getName()
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null, 
                producto.getPrecioBase(),
                producto.getStockActual()
        );
        
        // Asignar las listas de DTOs anidados

        dto.setResenas(DTOresena);


        return dto;
    }



    /**
     * Guarda una nueva reseña para un producto específico, asignándola al usuario logueado.
     * @param idProducto El ID del producto.
     * @param resena DTO que contiene solo el comentario (input).
     * @return El DtoResena guardado, incluyendo el idResena y la fecha asignada (output).
     * @throws EntityNotFoundException si el producto o el usuario no existen.
     */
    @Transactional
    public DtoResena guardarResena(Integer idProducto, DtoResena resena) {
        
        //  OBTENER EL USUARIO AUTENTICADO DE SPRING SECURITY
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); 
        
        // Buscar la entidad Usuario completa por el username (asumiendo que es el email o login)
        Usuario usuario = userRepository.findByemail(email) 
            .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado: " + email));
        
        //  Buscar el producto al que se asignará la reseña
        ProductoEntities producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + idProducto));
        
        // Crear la entidad Resena
        ResenaEntities nuevaResena = new ResenaEntities();
        
        // Asignar datos del DTO de entrada
        nuevaResena.setComentario(resena.getComentario()); 
        nuevaResena.setFechaCreacion(LocalDateTime.now());
        
        // Asignar relaciones
        nuevaResena.setProducto(producto);
        nuevaResena.setUsuario(usuario); 
        
        //  Guardar la entidad
        ResenaEntities reseñaGuardada = resenaRepository.save(nuevaResena);
        
        //  Mapear de vuelta al DTO para la respuesta (OUTPUT)
        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
        
        // Retorna el DTO de salida completo
        return new DtoResena(
                reseñaGuardada.getIdResena(),
                nombreCompleto, 
                reseñaGuardada.getComentario(),
                reseñaGuardada.getFechaCreacion()
        );
    }

    /**
     * Obtiene la lista de reseñas de un producto específico, mapeadas a DTO.
     * @param idProducto 
     * @return Lista de DtoResena.
     * @throws EntityNotFoundException 
     */
    @Transactional(readOnly = true)
    public List<DtoResena> getResenasByProductoId(Integer idProducto) {
        
        //Buscar la entidad Producto.
        ProductoEntities producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + idProducto));
        
        //Mapear las Reseñas a DTOs.
        return producto.getResenas().stream()
                .map(this::mapToResenaDTO)
                .collect(Collectors.toList());
    }

    

        private DtoResena mapToResenaDTO(ResenaEntities resena) {
        
        String nombreCompleto;
        if (resena.getUsuario() != null) {
            // Concatena nombres y apellidos del usuario
            nombreCompleto = resena.getUsuario().getNombres() + " " + resena.getUsuario().getApellidos();
        } else {
             nombreCompleto = "Usuario Desconocido"; 
        }

        return new DtoResena(
                resena.getIdResena(),          
                nombreCompleto,              
                resena.getComentario(),      
                resena.getFechaCreacion()    
        );
    
    }



    

    
}
  

