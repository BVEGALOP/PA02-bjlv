package pe.edu.isil.inventario.dto;

public class StockResponse {
  private Long productoId;
  private String codigo;
  private int stock;
  private boolean disponible;

  public StockResponse() {
  }

  public StockResponse(Long productoId, String codigo, int stock, boolean disponible) {
    this.productoId = productoId;
    this.codigo = codigo;
    this.stock = stock;
    this.disponible = disponible;
  }

  public Long getProductoId() {
    return productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getStock() {
    return stock;
  }

  public boolean isDisponible() {
    return disponible;
  }
}
