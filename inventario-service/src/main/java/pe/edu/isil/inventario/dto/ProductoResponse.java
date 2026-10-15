package pe.edu.isil.inventario.dto;

import java.math.BigDecimal;

/**
 * Contrato REST del catálogo de productos.
 */
public class ProductoResponse {

  private Long id;
  private String codigo;
  private String nombre;
  private BigDecimal precio;
  private int stock;
  private Long version;

  public ProductoResponse() {
  }

  public ProductoResponse(
      Long id,
      String codigo,
      String nombre,
      BigDecimal precio,
      int stock,
      Long version
  ) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
    this.precio = precio;
    this.stock = stock;
    this.version = version;
  }

  public Long getId() {
    return id;
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

  public int getStock() {
    return stock;
  }

  public Long getVersion() {
    return version;
  }
}
