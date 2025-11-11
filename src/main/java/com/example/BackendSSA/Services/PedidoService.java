package com.example.BackendSSA.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import com.example.BackendSSA.Dtos.DtoDetallePedido;
import com.example.BackendSSA.Dtos.DtoPedido;
import com.example.BackendSSA.Entities.DetallePedidoEntities;
import com.example.BackendSSA.Entities.EstadoEntities;
import com.example.BackendSSA.Entities.PedidoEntities;
import com.example.BackendSSA.Entities.ProductoEntities;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.EstadoRepository;
import com.example.BackendSSA.Repositories.PedidoRepository;
import com.example.BackendSSA.Repositories.ProductoRepository;
import com.example.BackendSSA.Repositories.UserRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UserRepository userRepository;
    private final ProductoRepository productoRepository;
    private final EstadoRepository estadoRepository;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, UserRepository userRepository, ProductoRepository productoRepository, EstadoRepository estadoRepository){
        this.pedidoRepository = pedidoRepository;
        this.userRepository = userRepository;
        this.productoRepository = productoRepository;
        this.estadoRepository = estadoRepository;
    }

    /**
     * Crea y persiste un nuevo PedidoEntity, obteniendo el usuario autenticado de Spring Security.
     * El idUsuario NO se recibe en el DTO, se extrae del contexto de seguridad.
     * @param request El DTO con los datos del pedido (sin idUsuario).
     * @return La entidad PedidoEntity guardada con su ID.
     * @throws ResourceNotFoundException si el producto o estado inicial no existen.
     * @throws UsernameNotFoundException si el usuario autenticado no existe en la base de datos.
     */
    @Transactional
    public PedidoEntities crearPedido(DtoPedido request) {
        
        // 1. OBTENER EL USUARIO AUTENTICADO DE SPRING SECURITY
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asumiendo que el "principal" (name) de la autenticación es el email o username
        String email = authentication.getName();
        
        // Buscar la entidad Usuario completa por el username (asumiendo que es el email o login)
        Usuario usuario = userRepository.findByemail(email) 
            .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado: " + email));
        
        // 2. Obtener Estado Inicial y Validar Existencia
        
        // Obtener Estado Inicial (Asumimos que existe un estado "PENDIENTE")
        EstadoEntities estadoInicial = estadoRepository.findByNombreEstado("PENDIENTE")
                .orElseThrow(() -> new EntityNotFoundException("Estado inicial 'PENDIENTE' no encontrado en la base de datos."));


        


        // 3. Mapear y configurar PedidoEntity (Encabezado)
        PedidoEntities pedido = new PedidoEntities();
        pedido.setUsuario(usuario); // Asignación de la Entidad Usuario (segura desde Security)
        pedido.setEstado(estadoInicial); // Asignación de la Entidad Estado
        pedido.setDireccionEnvio(request.getDireccionEnvio());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setCulqiToken(request.getCulquitoken());
        
        List<DetallePedidoEntities> detallesGuardados = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        // 4. Procesar los Detalles del Pedido (Cuerpo)
        
        for (DtoDetallePedido DtoDetallePedido : request.getDetalles()) {
            
            // Buscar el Producto para validar su existencia y obtener el precio real
            ProductoEntities producto = productoRepository.findById(DtoDetallePedido.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + DtoDetallePedido.getIdProducto()));


            // *** VALIDACIÓN DE STOCK ***
            if (producto.getStockActual() < DtoDetallePedido.getCantidad()) {
                 throw new RuntimeException("Stock insuficiente. El producto '" + producto.getNombre() + "' solo tiene " + producto.getStockActual() + " unidades.");
            }

            // Crear DetallePedidoEntity
            DetallePedidoEntities detalle = new DetallePedidoEntities();
            
            // Establecer las relaciones ManyToOne
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            
            detalle.setCantidad(DtoDetallePedido.getCantidad());
            
            // Usar el precio de la base de datos (Entidad Producto) por seguridad
            BigDecimal precioUnitario = producto.getPrecioBase(); 
            detalle.setPrecioUnitarioCompra(precioUnitario);

            // Actualizar el total del pedido
            BigDecimal subTotalDetalle = precioUnitario.multiply(BigDecimal.valueOf(DtoDetallePedido.getCantidad()));
            totalPedido = totalPedido.add(subTotalDetalle);

            detallesGuardados.add(detalle);

            // *** DESCONTAR STOCK Y PERSISTIR ***
            producto.setStockActual(producto.getStockActual() - DtoDetallePedido.getCantidad());
            productoRepository.save(producto);
        }

        // 5. Finalizar Pedido y Persistir
        
        pedido.setTotal(totalPedido);
        pedido.setDetalles(detallesGuardados);

        // Se guarda el Pedido y, por la configuración de cascada, también los Detalles.
        PedidoEntities pedidoGuardado = pedidoRepository.save(pedido);
        
        // 6. Mapear a DTO de salida (si tu controlador lo requiere)
        // Aquí asumiremos que se retorna la entidad PedidoEntity completa, o podrías crear un PedidoResponseDTO si lo prefieres.
        return pedidoGuardado;
    }
    
    /**
     * Busca un pedido por ID.
     * @param idPedido El ID del pedido a buscar.
     * @return La entidad PedidoEntity.
     * @throws ResourceNotFoundException si el pedido no es encontrado.
     */
    @Transactional(readOnly = true)
    public PedidoEntities buscarPedidoPorId(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con ID: " + idPedido));
    }

    




    
}
