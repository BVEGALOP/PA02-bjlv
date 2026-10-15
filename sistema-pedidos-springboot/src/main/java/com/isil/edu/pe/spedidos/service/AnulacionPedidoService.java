package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;

/**
 * Contrato de negocio para la anulación de pedidos y la devolución de stock
 * coordinada con inventario-service.
 */
public interface AnulacionPedidoService {

  /**
   * Ejecuta la anulación de un pedido registrado:
   * 1. Valida que el pedido exista y tenga estado REGISTRADO.
   * 2. Solicita la restitución de stock a inventario-service mediante HTTP REST.
   * 3. Actualiza el estado del pedido a ANULADO en H2.
   * 4. Registra la auditoría de anulación en H2.
   *
   * @param pedidoId identificador del pedido a anular
   * @param motivo motivo opcional de la anulación
   * @return registro de anulación creado
   */
  AnulacionPedido anularPedido(Long pedidoId, String motivo);

  /**
   * Consulta todas las anulaciones de pedidos registradas.
   *
   * @return lista de anulaciones
   */
  List<AnulacionPedido> listarAnulaciones();
}
