package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pedido realizado por un cliente.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String cliente;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(name = "producto_nombre", nullable = false, length = 120)
  private String productoNombre;

  @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
  private BigDecimal precioUnitario;

  @Column(nullable = false)
  private int cantidad;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PedidoEstado estado = PedidoEstado.REGISTRADO;

  public Pedido() {
  }

  public Pedido(String cliente, String productoCodigo, String productoNombre, BigDecimal precioUnitario,
      int cantidad, BigDecimal total) {
    this.cliente = cliente;
    this.productoCodigo = productoCodigo;
    this.productoNombre = productoNombre;
    this.precioUnitario = precioUnitario;
    this.cantidad = cantidad;
    this.total = total;
    this.fecha = LocalDateTime.now();
  }

  public Pedido(Long id, String cliente, String productoCodigo, String productoNombre, BigDecimal precioUnitario,
      int cantidad, BigDecimal total, LocalDateTime fecha) {
    this.id = id;
    this.cliente = cliente;
    this.productoCodigo = productoCodigo;
    this.productoNombre = productoNombre;
    this.precioUnitario = precioUnitario;
    this.cantidad = cantidad;
    this.total = total;
    this.fecha = fecha;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCliente() {
    return cliente;
  }

  public void setCliente(String cliente) {
    this.cliente = cliente;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public void setProductoCodigo(String productoCodigo) {
    this.productoCodigo = productoCodigo;
  }

  public String getProductoNombre() {
    return productoNombre;
  }

  public void setProductoNombre(String productoNombre) {
    this.productoNombre = productoNombre;
  }

  public BigDecimal getPrecioUnitario() {
    return precioUnitario;
  }

  public void setPrecioUnitario(BigDecimal precioUnitario) {
    this.precioUnitario = precioUnitario;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public PedidoEstado getEstado() {
    return estado;
  }

  public void setEstado(PedidoEstado estado) {
    this.estado = estado;
  }
}