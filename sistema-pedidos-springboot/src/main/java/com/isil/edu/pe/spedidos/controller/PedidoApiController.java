package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.controller.dto.PedidoResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.PedidoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador HTTP de ejemplo utilizado para demostrar cómo una misma
 * dependencia de negocio puede ser reutilizada desde diferentes componentes
 * administrados por Spring.
 *
 * <p>El objetivo pedagógico de esta clase en la Sesión 5 no es desarrollar
 * todavía los conceptos de servicios REST en profundidad. Su finalidad es
 * hacer observable la Inyección de Dependencias.</p>
 *
 * <p>El controlador depende de la abstracción {@link PedidoService} y recibe
 * su implementación mediante Constructor Injection.</p>
 *
 * <pre>
 * Petición HTTP
 *      |
 *      v
 * PedidoApiController
 *      |
 *      | Constructor Injection
 *      v
 * PedidoService
 *      |
 *      v
 * InventarioClient
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/orders")
public class PedidoApiController {

  private final PedidoService pedidoService;

  /**
   * Construye el controlador utilizando el servicio proporcionado
   * por el contenedor IoC de Spring.
   *
   * @param pedidoService servicio de negocio de pedidos
   */
  public PedidoApiController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }

  /**
   * Obtiene los productos disponibles desde Inventario.
   *
   * <p>El método no conoce cómo se realiza la consulta externa.
   * Únicamente delega la operación en {@link PedidoService}.</p>
   *
   * @return productos disponibles
   */
  @GetMapping
  public List<PedidoResponse> listarPedidos() {
    return pedidoService.listarPedidos()
        .stream()
        .map(PedidoResponse::from)
        .toList();
  }
}
