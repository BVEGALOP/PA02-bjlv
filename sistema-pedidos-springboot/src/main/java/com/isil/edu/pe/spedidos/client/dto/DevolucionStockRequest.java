package com.isil.edu.pe.spedidos.client.dto;

/**
 * DTO para enviar la solicitud de devolución de stock a inventario-service.
 */
public class DevolucionStockRequest {

  private int cantidad;

  public DevolucionStockRequest() {
  }

  public DevolucionStockRequest(int cantidad) {
    this.cantidad = cantidad;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }
}
