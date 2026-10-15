package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductoInventarioTest {

  @Test
  void debeDescontarStock() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        5
    );

    producto.reservarStock(2);

    assertEquals(3, producto.getStock());
    assertEquals(new BigDecimal("2500.00"), producto.getPrecio());
  }

  @Test
  void noDebePermitirReservarMasDelStockDisponible() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        1
    );

    assertThrows(IllegalStateException.class, () -> producto.reservarStock(2));
    assertEquals(1, producto.getStock());
  }

  /**
   * Prueba obligatoria PA2 - Devolución válida:
   * Una devolución de cantidad positiva incrementa el stock exactamente en esa cantidad (8 + 2 = 10).
   */
  @Test
  void debeIncrementarStockEnDevolucionValida() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        8
    );

    producto.devolverStock(2);

    assertEquals(10, producto.getStock(), "El stock debe incrementarse exactamente en la cantidad devuelta (8 + 2 = 10).");
  }

  /**
   * Prueba obligatoria PA2 - Devolución inválida:
   * Una devolución de 0 o cantidad negativa se rechaza y no cambia el stock.
   */
  @Test
  void noDebePermitirDevolucionConCantidadMenorOIgualACero() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        8
    );

    // Caso cantidad 0
    assertThrows(
        IllegalArgumentException.class,
        () -> producto.devolverStock(0),
        "Debe rechazar devolución con cantidad 0"
    );

    // Caso cantidad negativa
    assertThrows(
        IllegalArgumentException.class,
        () -> producto.devolverStock(-3),
        "Debe rechazar devolución con cantidad negativa"
    );

    assertEquals(8, producto.getStock(), "El stock no debe modificarse si la devolución es rechazada.");
  }
}
