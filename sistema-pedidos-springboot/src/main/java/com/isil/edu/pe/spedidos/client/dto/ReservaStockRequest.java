package com.isil.edu.pe.spedidos.client.dto;

/**
 * Contrato enviado a POST /inventario/productos/codigo/{codigo}/reservas.
 */
public class ReservaStockRequest {

  private Integer cantidad;

  public ReservaStockRequest() {
  }

  public ReservaStockRequest(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
