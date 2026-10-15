package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

/**
 * Contrato de cliente para la devolución de stock en inventario-service.
 */
public interface DevolucionStockClient {

  /**
   * Solicita la devolución de stock de un producto dado su código y la cantidad devuelta.
   *
   * @param codigo código del producto
   * @param cantidad cantidad de unidades a restituir al inventario
   * @return información de la devolución con el nuevo stock restante
   */
  DevolucionStockResponse devolverStock(String codigo, int cantidad);
}
