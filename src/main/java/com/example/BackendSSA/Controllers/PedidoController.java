package com.example.BackendSSA.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Dtos.DtoPedido;
import com.example.BackendSSA.Entities.PedidoEntities;
import com.example.BackendSSA.Services.PedidoService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    
    private final PedidoService pedidoService;


    @Autowired
    public PedidoController(PedidoService pedidoService ){
        this.pedidoService = pedidoService;
    }

    /**
     * Endpoint para crear un nuevo pedido.
     * El usuario se toma automáticamente del contexto de seguridad de Spring Security.
     * Retorna el pedido creado y un estado HTTP 201 (Created).
     *
     * URL: POST /api/pedidos
     * Cuerpo: DtoPedido (que ya no tiene idUsuario)
     */
    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody DtoPedido request) {
        try {
            PedidoEntities nuevoPedido = pedidoService.crearPedido(request);
            // Retorna el objeto PedidoEntities completo guardado
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED); 
        } catch (EntityNotFoundException e) {
            // Captura si el usuario, producto o estado inicial no fueron encontrados
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // Para otros errores no controlados (ej. problemas de base de datos)
            return new ResponseEntity<>("Error al procesar el pedido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para obtener un pedido por su ID.
     * Nota: Deberías agregar aquí una lógica de seguridad para que solo el dueño o un admin 
     * pueda ver el pedido.
     * * URL: GET /api/pedidos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPedidoPorId(@PathVariable("id") Integer idPedido) {
        try {
            PedidoEntities pedido = pedidoService.buscarPedidoPorId(idPedido);
            return new ResponseEntity<>(pedido, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // Captura si el pedido no fue encontrado
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al buscar el pedido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
