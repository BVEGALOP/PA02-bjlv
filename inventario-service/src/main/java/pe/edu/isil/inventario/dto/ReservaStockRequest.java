package pe.edu.isil.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReservaStockRequest {

  @NotNull(message = "La cantidad es obligatoria.")
  @Positive(message = "La cantidad debe ser mayor que cero.")
  private Integer cantidad;

  public ReservaStockRequest() {
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
