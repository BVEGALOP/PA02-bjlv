package pe.edu.isil.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/**
 * Entidad que representa el catálogo y el stock disponible de un producto.
 */
@Entity
@Table(name = "producto_inventario")
public class ProductoInventario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String codigo;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal precio;

  @Column(nullable = false)
  private int stock;

  /**
   * Versionado optimista: permite detectar actualizaciones concurrentes del stock.
   */
  @Version
  @Column(nullable = false)
  private Long version;

  protected ProductoInventario() {
    // Requerido por Jakarta Persistence.
  }

  public ProductoInventario(String codigo, String nombre, BigDecimal precio, int stock) {
    if (codigo == null || codigo.isBlank()) {
      throw new IllegalArgumentException("El código es obligatorio.");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre es obligatorio.");
    }
    if (precio == null || precio.signum() < 0) {
      throw new IllegalArgumentException("El precio debe ser mayor o igual a cero.");
    }
    if (stock < 0) {
      throw new IllegalArgumentException("El stock no puede ser negativo.");
    }

    this.codigo = codigo.trim();
    this.nombre = nombre.trim();
    this.precio = precio;
    this.stock = stock;
  }

  public void reservarStock(int cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }
    if (stock < cantidad) {
      throw new IllegalStateException(
          "Stock insuficiente. Disponible: " + stock + ", solicitado: " + cantidad + "."
      );
    }
    stock -= cantidad;
  }

  public void devolverStock(int cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad a devolver debe ser mayor que cero.");
    }
    this.stock += cantidad;
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
