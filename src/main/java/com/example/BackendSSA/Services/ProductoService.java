package com.example.BackendSSA.Services; 

import com.example.BackendSSA.Dtos.DtoDetalleProducto;
import com.example.BackendSSA.Dtos.DtoProducto;
//import com.example.BackendSSA.Dtos.DtoPreferencias;
import com.example.BackendSSA.Dtos.DtoResena;
import com.example.BackendSSA.Entities.PreferenciasEntities;
import com.example.BackendSSA.Entities.ProductoEntities; 
import com.example.BackendSSA.Entities.ResenaEntities; 
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.PreferenciasRepository;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.example.BackendSSA.Repositories.ResenaRepository;
import com.example.BackendSSA.Repositories.UserRepository;
import org.springframework.security.core.Authentication;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional; 

import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ResenaRepository resenaRepository;
    private final UserRepository userRepository;
    private final PreferenciasRepository preferenciasRepository;
    //private final PreferenciasService preferenciasService;

    
    // Inyección de dependencias por constructor
    @Autowired
    public ProductoService(ProductoRepository productoRepository, ResenaRepository resenaRepository, UserRepository userRepository, PreferenciasRepository preferenciasRepository /*PreferenciasService preferenciasService*/) {
        this.productoRepository = productoRepository;
        this.resenaRepository = resenaRepository;
        this.userRepository = userRepository;
        this.preferenciasRepository = preferenciasRepository;
       // this.preferenciasService = preferenciasService;
    }
    
    /**
     * Busca los productos más relevantes en la base de datos basándose en un criterio de búsqueda.
     * La búsqueda se realiza sobre campos clave (nombre, descripción, categoría).
     * @param query El término de búsqueda del usuario.
     * @return Una lista de hasta 5 DtoProducto relevantes.
     */
    // 🚀 IMPLEMENTACIÓN REAL DE LA BÚSQUEDA PARA EL CHATBOT
    /*public List<DtoProducto> buscarPorCriterio(String query) {
        if (query == null || query.isBlank()) {
            return List.of(); // Devolver lista vacía si la consulta es nula o vacía
        }

        // 1. Normalizar el query (Convertir a minúsculas y limpiar)
        String cleanedQuery = query.toLowerCase().trim();
        
        // 2. Limitar a los 5 resultados más relevantes (Pageable: página 0, 5 elementos)
        Pageable pageable = PageRequest.of(0, 5); 

        // 3. Llamar al Repositorio con el criterio normalizado
        // Importante: Este método DEBE EXISTIR en ProductoRepository con la sintaxis exacta:
        // List<ProductoEntities> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String n, String d, String c, Pageable p);
        List<ProductoEntities> productos = productoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrCategoriaContainingIgnoreCase(
            cleanedQuery, // Buscar en Nombre
            cleanedQuery, // Buscar en Descripción
            cleanedQuery, // Buscar en Categoría
            pageable
        );
        
        // 4. Mapear las entidades Producto a DtoProducto
        return productos.stream()
            .map(this::mapToChatbotDto) // Usamos un método auxiliar específico para el chatbot
            .collect(Collectors.toList());
    }*/

    /**
     * Busca los productos más relevantes en la base de datos basándose en un criterio de búsqueda.
     * La búsqueda se realiza sobre campos clave (nombre, descripción, categoría).
     * @param query El término de búsqueda del usuario.
     * @return Una lista de hasta 5 DtoProducto relevantes.
     */
    public List<DtoProducto> buscarPorCriterio(String query) {
        if (query == null || query.isBlank()) {
            return List.of(); 
        }

        String cleanedQuery = query.toLowerCase().trim();
        
        // 2. Limitar a los 5 resultados más relevantes
        Pageable pageable = PageRequest.of(0, 5); 

        // 3. LLAMADA AL NUEVO MÉTODO DEL REPOSITORY (más robusto)
        List<ProductoEntities> productos = productoRepository.buscarPorCriterioEnCampos(
            cleanedQuery, // Solo se necesita el query una vez
            pageable
        );
        
        // 4. Mapear las entidades Producto a DtoProducto
        return productos.stream()
            .map(this::mapToChatbotDto) 
            .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para mapear la entidad Producto a DtoProducto.
     * Diseñado para el Chatbot: solo incluye los campos necesarios (nombre, precio, descripcion corta)
     * para que Groq genere una respuesta útil.
     * @param producto La entidad Producto de la base de datos.
     * @return El DTO de producto.
     */
    private DtoProducto mapToChatbotDto(ProductoEntities producto) {
        // Asumiendo que DtoProducto tiene un constructor o setters para estos campos
        DtoProducto dto = new DtoProducto();
        // ⚠️ Asumo que tienes los siguientes getters en ProductoEntities y setters en DtoProducto
        // dto.setSku(producto.getSku()); 
        dto.setNombre(producto.getNombre());
        // dto.setDescripcionCorta(producto.getDescripcionCorta()); // Si existe una descripción corta
        dto.setPrecioBase(producto.getPrecioBase());
        // dto.setUrlImagen(producto.getUrlImagen()); 
        dto.setImagenUrl(producto.getImagenUrl());
        // Si necesitas descripción completa o algún otro campo para la respuesta del bot:
        dto.setDescripcion(producto.getDescripcion()); // Usar la descripción completa si no hay corta
        
        return dto;
    }

    /**
     * Obtiene productos ordenados por relevancia de personalización 
     * basada en las preferencias del usuario.
     * @param userId ID del usuario logueado.
     * @return Lista de DtoProducto ordenada por score de personalización.
     */
    @Transactional(readOnly = true)
    public List<DtoProducto> getProductosPersonalizados(Integer userId) { // 🛑 Cambiamos a Integer para coincidir con tu ID
        
        // 1. OBTENER LAS PREFERENCIAS DEL USUARIO
        Optional<PreferenciasEntities> prefsOpt = preferenciasRepository.findByUsuarioIdUsuario(userId);

        // Si el usuario no tiene preferencias, devolvemos el catálogo completo sin ordenar.
        if (prefsOpt.isEmpty()) {
            return productoRepository.findAll().stream()
                .map(DtoProducto::fromEntity) 
                .collect(Collectors.toList()); 
        }

        PreferenciasEntities userPrefs = prefsOpt.get();
        
        // 2. OBTENER TODOS LOS PRODUCTOS
        List<ProductoEntities> allProducts = productoRepository.findAll();
        
        // 3. CALCULAR SCORE, FILTRAR Y ORDENAR
        List<DtoProducto> personalizedList = allProducts.stream()
            .map(producto -> {
                int score = calculateMatchScore(producto, userPrefs);
                
                // Solo incluimos productos que tienen al menos 1 punto de coincidencia
                if (score > 0) {
                    DtoProducto dto = DtoProducto.fromEntity(producto); // Usamos el mapper estático
                    dto.setScore(score); 
                    return dto;
                }
                return null; // El producto no coincide con ninguna preferencia
            })
            .filter(Objects::nonNull) // Elimina los productos con score 0 (los nulos)
            .sorted(Comparator.comparing(DtoProducto::getScore).reversed()) // Ordenar por score descendente
            .collect(Collectors.toList());

        return personalizedList;
    }

    /**
     * Calcula la puntuación de coincidencia entre un producto y las preferencias del usuario.
     */
    private int calculateMatchScore(ProductoEntities p, PreferenciasEntities prefs) {
        int score = 0;
        
        // Suponemos que los campos de preferencias son Strings separadas por coma (CSV)
        // Ejemplo: "Rojo,Azul" -> ["Rojo", "Azul"]

        // Arrays para listas de preferencias (manejo de Null y separación por coma)
        List<String> userColors = prefs.getColoresFavoritos() != null ? 
                                  Arrays.asList(prefs.getColoresFavoritos().split(",")) : List.of();
        List<String> userStyles = prefs.getEstilos() != null ? 
                                  Arrays.asList(prefs.getEstilos().split(",")) : List.of();
        List<String> userHobbies = prefs.getHobbies() != null ? 
                                   Arrays.asList(prefs.getHobbies().split(",")) : List.of();
        List<String> userInterests = prefs.getIntereses() != null ? 
                                     Arrays.asList(prefs.getIntereses().split(",")) : List.of();

        // --- PESOS DE PUNTUACIÓN (AJUSTAR SEGÚN NECESIDAD) ---
        
        // 1. Coincidencia de Color (Peso = 3)
        // Usamos .contains para ver si el color del producto está en la lista de colores favoritos
        if (p.getColor() != null && userColors.contains(p.getColor())) {
            score += 3;
        }
        
        // 2. Coincidencia de Estilo (Peso = 2)
        if (p.getEstilo() != null && userStyles.contains(p.getEstilo())) {
            score += 2;
        }

        // 3. Coincidencia de Profesión (Peso = 1)
        if (prefs.getProfesion() != null && p.getProfesion() != null && 
            prefs.getProfesion().equalsIgnoreCase(p.getProfesion())) {
            score += 1;
        }
        
        // 4. Coincidencia de Hobbie/Interés (Peso = 1) - Revisar si el producto tiene alguno de los tags
        if (p.getHobbie() != null && userHobbies.contains(p.getHobbie())) { 
            score += 1;
        }

        if (p.getInteres() != null && userInterests.contains(p.getInteres())) {
            score += 1;
        }
        
        return score;
    }
    
    
    /* 

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

        dto.setUrlImagen(producto.getImagenUrl());
        
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
  

