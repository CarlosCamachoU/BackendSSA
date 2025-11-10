package com.example.BackendSSA.Services;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BackendSSA.Dtos.DtoDetalleProducto;

import com.example.BackendSSA.Dtos.DtoResena;
import com.example.BackendSSA.Entities.ProductoEntities;

import com.example.BackendSSA.Entities.ResenaEntities;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper; // Para manejar la conversión JSON

    @Autowired
    public ProductoService(ProductoRepository productoRepository, ObjectMapper objectMapper) {
        this.productoRepository = productoRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Busca un producto por ID y lo convierte a un DTO detallado, 
     * incluyendo la deserialización de los atributos JSON.
     * * @param id El ID del producto.
     * @return ProductoDetalleDTO con toda la información.
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
        
       

        // 4. Deserializar el String JSON a Map<String, Object>
        Map<String, Object> atributosMap = deserializeJson(producto.getAtributosJson());

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
                producto.getStockActual(),
                producto.getNumeroResenas(),
                producto.getImagenPrincipalUrl(),
                atributosMap
        );
        
        // Asignar las listas de DTOs anidados

        dto.setResenas(DTOresena);


        return dto;
    }
    
    /**
     * Helper para deserializar el String JSON.
     */
    private Map<String, Object> deserializeJson(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("null")) {
            return Map.of(); // Devuelve un mapa vacío si el JSON es nulo o vacío
        }
        try {
            // ObjectMapper convierte el String JSON en un Map
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            // Manejo de errores: loggea y devuelve un mapa vacío para no romper la app
            System.err.println("Error al deserializar JSON de atributos: " + e.getMessage());
            return Map.of(); 
        }
    }

   

    /**
     * Mapea ResenaEntities a ResenaDTO.
     */
    private DtoResena mapToResenaDTO(ResenaEntities resena) {
        // Se asume que la entidad UsuarioEntities tiene un método getNombre()
        String nombreUsuario = resena.getUsuario() != null ? resena.getUsuario().getNombres() : "Usuario Anónimo";
        
        return new DtoResena(
                resena.getComentario(),
                resena.getFechaCreacion(),
                nombreUsuario
        );
    }
  
    
}
