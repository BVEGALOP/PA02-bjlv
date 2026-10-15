package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.exception.AnulacionDuplicadaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar la anulación de pedidos y trazabilidad de devoluciones.
 *
 * <p>Endpoints expuestos:</p>
 * <ul>
 *   <li>POST /api/v1/anulaciones: anula un pedido y devuelve el stock a inventario-service.</li>
 *   <li>GET /api/v1/anulaciones: lista todas las anulaciones registradas en H2.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

  private final AnulacionPedidoService anulacionPedidoService;

  /**
   * Inyección de dependencias mediante constructor (Constructor Injection).
   *
   * @param anulacionPedidoService servicio de negocio de anulación de pedidos
   */
  public AnulacionPedidoController(AnulacionPedidoService anulacionPedidoService) {
    this.anulacionPedidoService = anulacionPedidoService;
  }

  /**
   * Procesa la solicitud de anulación de un pedido.
   *
   * @param request DTO con el ID del pedido y motivo opcional
   * @return DTO con la anulación registrada
   */
  @PostMapping
  public ResponseEntity<AnulacionResponse> anularPedido(
      @RequestBody AnulacionRequest request,
      HttpServletRequest servletRequest) {

    if (request == null || request.getPedidoId() == null) {
      throw new PedidoException("El campo pedidoId es obligatorio en el cuerpo de la solicitud.");
    }

    AnulacionPedido anulacion = anulacionPedidoService.anularPedido(
        request.getPedidoId(),
        request.getMotivo()
    );

    AnulacionResponse response = AnulacionResponse.from(anulacion);
    URI location = URI.create(servletRequest.getRequestURI() + "/" + anulacion.getId());

    return ResponseEntity.created(location).body(response);
  }

  /**
   * Lista todas las anulaciones registradas en la base de datos.
   *
   * @return lista de anulaciones
   */
  @GetMapping
  public ResponseEntity<List<AnulacionResponse>> listarAnulaciones() {
    List<AnulacionResponse> anulaciones = anulacionPedidoService.listarAnulaciones()
        .stream()
        .map(AnulacionResponse::from)
        .toList();

    return ResponseEntity.ok(anulaciones);
  }

  // =========================================================================
  // Manejo de excepciones para respuestas HTTP semánticas y controladas
  // =========================================================================

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ResponseEntity<Map<String, Object>> handlePedidoNoEncontrado(
      PedidoNoEncontradoException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(AnulacionDuplicadaException.class)
  public ResponseEntity<Map<String, Object>> handleAnulacionDuplicada(
      AnulacionDuplicadaException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(InventarioNoDisponibleException.class)
  public ResponseEntity<Map<String, Object>> handleInventarioNoDisponible(
      InventarioNoDisponibleException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(InventarioClientException.class)
  public ResponseEntity<Map<String, Object>> handleInventarioClientError(
      InventarioClientException ex,
      HttpServletRequest request) {
    HttpStatus status = HttpStatus.resolve(ex.getStatus());
    if (status == null) {
      status = HttpStatus.BAD_REQUEST;
    }
    return buildErrorResponse(status, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(PedidoException.class)
  public ResponseEntity<Map<String, Object>> handlePedidoException(
      PedidoException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status,
      String message,
      String path) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", path);
    return ResponseEntity.status(status).body(body);
  }
}
