package com.example.BackendSSA.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.EntityNotFoundException;

import com.example.BackendSSA.Dtos.DtoDetallePedido;
import com.example.BackendSSA.Dtos.DtoDetallePedidoRespuesta;
import com.example.BackendSSA.Dtos.DtoPedido;
import com.example.BackendSSA.Dtos.DtoPedidoRespuesta;
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
    public PedidoService(
            PedidoRepository pedidoRepository,
            UserRepository userRepository,
            ProductoRepository productoRepository,
            EstadoRepository estadoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.userRepository = userRepository;
        this.productoRepository = productoRepository;
        this.estadoRepository = estadoRepository;
    }

    // ---------------- EXISTENTE: crear pedido (flujo anterior) ----------------

    @Transactional
    public PedidoEntities crearPedido(DtoPedido request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = userRepository.findByemail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado: " + email));

        EstadoEntities estadoInicial = estadoRepository.findByNombreEstado("PENDIENTE")
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estado inicial 'PENDIENTE' no encontrado en la base de datos."));

        PedidoEntities pedido = new PedidoEntities();
        pedido.setUsuario(usuario);
        pedido.setEstado(estadoInicial);
        pedido.setDireccionEnvio(request.getDireccionEnvio());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDni(request.getDni());
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setIdTransaccionCulqi(request.getIdTransaccionCulqi()); // o culqi / stripe según tu DTO

        // 🔹 Aquí calculas el número de pedido por cliente
        long pedidosPrevios = pedidoRepository.countByUsuario_IdUsuario(usuario.getIdUsuario());
        int numeroPedidoCliente = (int) pedidosPrevios + 1;
        pedido.setNumeroPedidoCliente(numeroPedidoCliente);
        
        List<DetallePedidoEntities> detallesGuardados = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        for (DtoDetallePedido dtoDetallePedido : request.getDetalles()) {

            ProductoEntities producto = productoRepository.findById(dtoDetallePedido.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con ID: " + dtoDetallePedido.getIdProducto()));

            if (producto.getStockActual() < dtoDetallePedido.getCantidad()) {
                throw new RuntimeException("Stock insuficiente. El producto '" + producto.getNombre()
                        + "' solo tiene " + producto.getStockActual() + " unidades.");
            }

            DetallePedidoEntities detalle = new DetallePedidoEntities();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(dtoDetallePedido.getCantidad());

            BigDecimal precioUnitario = producto.getPrecioBase();
            detalle.setPrecioUnitarioCompra(precioUnitario);

            BigDecimal subTotalDetalle = precioUnitario
                    .multiply(BigDecimal.valueOf(dtoDetallePedido.getCantidad()));
            totalPedido = totalPedido.add(subTotalDetalle);

            detallesGuardados.add(detalle);

            producto.setStockActual(producto.getStockActual() - dtoDetallePedido.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setTotal(totalPedido);
        pedido.setDetalles(detallesGuardados);

        PedidoEntities pedidoGuardado = pedidoRepository.save(pedido);
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public PedidoEntities buscarPedidoPorId(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con ID: " + idPedido));
    }

    // ---------- NUEVO: mapper para usar en historial / respuesta -----------

    private DtoPedidoRespuesta mapearPedidoADto(PedidoEntities pedido) {
        DtoPedidoRespuesta dto = new DtoPedidoRespuesta();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setNumeroPedidoCliente(pedido.getNumeroPedidoCliente());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setTotal(pedido.getTotal());
        dto.setDireccionEnvio(pedido.getDireccionEnvio());
        dto.setDni(pedido.getDni());
        dto.setMetodoPago(pedido.getMetodoPago());
        dto.setIdTransaccion(pedido.getIdTransaccionCulqi());

        List<DtoDetallePedidoRespuesta> detallesDto = pedido.getDetalles().stream().map(det -> {
            DtoDetallePedidoRespuesta d = new DtoDetallePedidoRespuesta();
            d.setIdProducto(det.getProducto().getIdProducto());
            d.setNombreProducto(det.getProducto().getNombre());
            d.setCantidad(det.getCantidad());
            d.setPrecioUnitario(det.getPrecioUnitarioCompra());
            return d;
        }).collect(Collectors.toList());

        dto.setDetalles(detallesDto);
        return dto;
    }

    // ---------- NUEVO: listar historial de pedidos del usuario logueado ----------

    @Transactional(readOnly = true)
    public List<DtoPedidoRespuesta> listarPedidosUsuarioActual() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = userRepository.findByemail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado: " + email));

        List<PedidoEntities> pedidos = pedidoRepository
                .findByUsuario_IdUsuarioOrderByFechaPedidoDesc(usuario.getIdUsuario());

        return pedidos.stream()
                .map(this::mapearPedidoADto)
                .collect(Collectors.toList());
    }

    // ---------- NUEVO: obtener detalle de un pedido específico del usuario ----------

    @Transactional(readOnly = true)
    public DtoPedidoRespuesta obtenerPedidoUsuarioActualPorId(Integer idPedido) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = userRepository.findByemail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logueado no encontrado: " + email));

        PedidoEntities pedido = pedidoRepository
                .findByIdPedidoAndUsuario_IdUsuario(idPedido, usuario.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido no encontrado o no pertenece al usuario actual. ID: " + idPedido));

        return mapearPedidoADto(pedido);
    }
}
