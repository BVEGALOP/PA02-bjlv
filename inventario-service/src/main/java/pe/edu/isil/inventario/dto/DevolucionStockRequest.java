package pe.edu.isil.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que transporta la solicitud de devolución de stock.
 */
public class DevolucionStockRequest {

  @NotNull(message = "La cantidad es obligatoria.")
  @Min(value = 1, message = "La cantidad a devolver debe ser mayor que cero.")
  private Integer cantidad;

  public DevolucionStockRequest() {
  }

  public DevolucionStockRequest(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
