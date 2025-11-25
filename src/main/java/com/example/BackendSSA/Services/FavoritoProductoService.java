package com.example.BackendSSA.Services;

import java.util.List;
import java.util.stream.Collectors;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BackendSSA.Dtos.DtoProducto;
import com.example.BackendSSA.Entities.FavoritosEntities;
import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.FavoritoProductoRepository;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.example.BackendSSA.Repositories.UserRepository;

@Service
public class FavoritoProductoService {
    
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private FavoritoProductoRepository favoritoproductoRepository; // Inyecta tu repositorio de la relación Favorito

 

    // Método auxiliar para obtener el usuario autenticado (se usaría de forma similar al PerfilController)
    private Usuario getUsuario(Integer idUsuario) {
        return userRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }


    /**
     * 💡 Lógica para añadir/eliminar (toggle) un producto de favoritos.
     */

    @Transactional
    public boolean toggleFavorito(Integer idUsuario, Integer idProducto) {
        Usuario usuario = getUsuario(idUsuario);
        ProductoEntities producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

        // Busca si ya existe la relación de favorito
        return favoritoproductoRepository.findByUsuarioAndProducto(usuario, producto)
                .map(favorito -> {
                    // Si existe, lo elimina (ELIMINAR)
                    favoritoproductoRepository.delete(favorito);
                    return false; // Retorna falso para indicar que fue eliminado
                })
                .orElseGet(() -> {
                    // Si no existe, lo crea (AÑADIR)
                    FavoritosEntities nuevoFavorito = new FavoritosEntities(usuario, producto);
                    favoritoproductoRepository.save(nuevoFavorito);
                    return true; // Retorna verdadero para indicar que fue añadido
                });
    }

    /**
     * Obtiene solo los IDs de los productos favoritos del usuario.
     */
    
    /**
     * Obtiene solo los IDs de los productos favoritos del usuario.
     */
    @Transactional(readOnly = true) // Este sí debe ser readOnly = true
    public List<Integer> obtenerIdsFavoritos(Integer idUsuario) {
        Usuario usuario = getUsuario(idUsuario);
        
        // Busca todos los objetos Favorito para el usuario
        List<FavoritosEntities> favoritos = favoritoproductoRepository.findByUsuario(usuario);

        // Mapea la lista de objetos Favorito a una lista de IDs de Producto
        return favoritos.stream()
                .map(favorito -> favorito.getProducto().getIdProducto())
                .collect(Collectors.toList());
    }
    


    // --- NUEVA LÓGICA DE DETALLES DE PRODUCTOS FAVORITOS ---
    
    /**
     * 💡 MÉTODO DE MAFEO MANUAL (SIMILAR A mapToResenaDTO en ProductoService)
     * Convierte la entidad ProductoEntities en el DTO simplificado (DtoProducto).
     */
    private DtoProducto mapToDtoProducto(ProductoEntities producto) {
        // Asumiendo que DtoProducto tiene campos como: idProducto, nombre, precioBase, urlImagen, etc.
        DtoProducto dto = new DtoProducto();
        
        dto.setIdproducto(producto.getIdProducto());
        dto.setNombre(producto.getNombre());
        dto.setPrecioBase(producto.getPrecioBase());
        
        // ¡Crucial para mostrar la imagen y evitar el S/. NaN!
        // Asumo que tienes el campo 'urlImagen' en ProductoEntities y en DtoProducto
        dto.setImagenUrl(producto.getImagenUrl()); 
        
        // Si necesitas la descripción, stock, o SKU para el DTO simplificado:
        //dto.setDescripcion(producto.getDescripcion());
        //dto.setStockActual(producto.getStockActual());
        //dto.setSku(producto.getSku());
        /* 
        // Si necesitas la categoría para el DTO simplificado:
        if (producto.getCategoria() != null) {
             dto.setCategoria(producto.getCategoria().getNombre());
        }*/
        
        return dto;
    }


    /**
     * Obtiene los detalles completos de todos los productos favoritos de un usuario.
     */
    @Transactional(readOnly = true)
    public List<DtoProducto> getDetallesProductosFavoritos(Integer usuarioId) {
        
        // 1. Obtener la lista de IDs favoritos del usuario (Reutilizando tu método existente)
        List<Integer> idProductosFavoritos = this.obtenerIdsFavoritos(usuarioId);
        
        // 2. Usar los IDs para obtener los objetos Producto completos
        // Es importante que ProductoEntities cargue los datos necesarios (como la urlImagen).
        List<ProductoEntities> productos = productoRepository.findAllById(idProductosFavoritos);
        
        // 3. Mapear las entidades Producto a DTOs usando el método auxiliar
        return productos.stream()
            .map(this::mapToDtoProducto) // <-- Usamos el mapeador manual
            .collect(Collectors.toList());
    }


}
