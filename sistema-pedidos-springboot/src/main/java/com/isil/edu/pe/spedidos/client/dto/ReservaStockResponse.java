package com.isil.edu.pe.spedidos.client.dto;

import java.math.BigDecimal;

/**
 * Contrato recibido después de reservar stock.
 */
public class ReservaStockResponse {

  private Long productoId;
  private String codigo;
  private String nombre;
  private BigDecimal precio;
  private int cantidadReservada;
  private int stockRestante;
  private Long version;

  public ReservaStockResponse() {
  }

  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
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

  public int getCantidadReservada() {
    return cantidadReservada;
  }

  public void setCantidadReservada(int cantidadReservada) {
    this.cantidadReservada = cantidadReservada;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setStockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
