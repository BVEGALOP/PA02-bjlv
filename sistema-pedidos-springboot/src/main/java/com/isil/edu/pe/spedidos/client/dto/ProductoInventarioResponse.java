package com.isil.edu.pe.spedidos.client.dto;

import java.math.BigDecimal;

/**
 * Contrato recibido desde GET /inventario/productos.
 */
public class ProductoInventarioResponse {

  private Long id;
  private String codigo;
  private String nombre;
  private BigDecimal precio;
  private int stock;
  private Long version;

  public ProductoInventarioResponse() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
