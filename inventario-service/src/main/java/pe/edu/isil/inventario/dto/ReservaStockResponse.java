package pe.edu.isil.inventario.dto;

import java.math.BigDecimal;

/**
 * Respuesta de una reserva de stock.
 *
 * <p>Incluye un snapshot de los datos comerciales del producto para que el
 * consumidor pueda registrar el pedido sin mantener un catálogo duplicado.</p>
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

  public ReservaStockResponse(
      Long productoId,
      String codigo,
      String nombre,
      BigDecimal precio,
      int cantidadReservada,
      int stockRestante,
      Long version
  ) {
    this.productoId = productoId;
    this.codigo = codigo;
    this.nombre = nombre;
    this.precio = precio;
    this.cantidadReservada = cantidadReservada;
    this.stockRestante = stockRestante;
    this.version = version;
  }

  public Long getProductoId() {
    return productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public int getCantidadReservada() {
    return cantidadReservada;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public Long getVersion() {
    return version;
  }
}
