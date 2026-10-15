package com.isil.edu.pe.spedidos.controller.dto;

/**
 * DTO para la solicitud de anulación de un pedido.
 */
public class AnulacionRequest {

  private Long pedidoId;
  private String motivo;

  public AnulacionRequest() {
  }

  public AnulacionRequest(Long pedidoId, String motivo) {
    this.pedidoId = pedidoId;
    this.motivo = motivo;
  }

  public Long getPedidoId() {
    return pedidoId;
  }

  public void setPedidoId(Long pedidoId) {
    this.pedidoId = pedidoId;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }
}
