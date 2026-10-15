package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta utilizado para exponer un pedido mediante JSON.
 *
 * <p>El {@code record} es apropiado para DTO inmutables cuyo propósito
 * principal es transportar información.</p>
 */
public record PedidoResponse(
    Long id,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal precioUnitario,
    int cantidad,
    BigDecimal total,
    LocalDateTime fecha,
    String estado
) {

  /**
   * Convierte una entidad {@link Pedido} en el DTO utilizado
   * por la capa HTTP.
   *
   * @param pedido entidad que se desea transformar
   * @return representación del pedido para la API
   */
  public static PedidoResponse from(Pedido pedido) {
    return new PedidoResponse(
        pedido.getId(),
        pedido.getCliente(),
        pedido.getProductoCodigo(),
        pedido.getProductoNombre(),
        pedido.getPrecioUnitario(),
        pedido.getCantidad(),
        pedido.getTotal(),
        pedido.getFecha(),
        pedido.getEstado() != null ? pedido.getEstado().name() : "REGISTRADO"
    );
  }
}
