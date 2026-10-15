package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Entidad que registra la anulación de un pedido y la trazabilidad de la
 * devolución de stock correspondiente a inventario-service.
 */
@Entity
@Table(
    name = "anulacion_pedido",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_anulacion_pedido_id", columnNames = {"pedido_id"})
    }
)
public class AnulacionPedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pedido_id", nullable = false, unique = true)
  private Long pedidoId;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(name = "cantidad_devuelta", nullable = false)
  private int cantidadDevuelta;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @Column(length = 255)
  private String motivo;

  public AnulacionPedido() {
  }

  public AnulacionPedido(Long pedidoId, String productoCodigo, int cantidadDevuelta, String motivo) {
    this.pedidoId = pedidoId;
    this.productoCodigo = productoCodigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.fecha = LocalDateTime.now();
    this.motivo = motivo;
  }

  public AnulacionPedido(Long id, Long pedidoId, String productoCodigo, int cantidadDevuelta, LocalDateTime fecha, String motivo) {
    this.id = id;
    this.pedidoId = pedidoId;
    this.productoCodigo = productoCodigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.fecha = fecha;
    this.motivo = motivo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPedidoId() {
    return pedidoId;
  }

  public void setPedidoId(Long pedidoId) {
    this.pedidoId = pedidoId;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public void setProductoCodigo(String productoCodigo) {
    this.productoCodigo = productoCodigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setCantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }
}
