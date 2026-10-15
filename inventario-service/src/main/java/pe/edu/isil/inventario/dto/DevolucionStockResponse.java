package pe.edu.isil.inventario.dto;

/**
 * DTO que transporta el resultado de una devolución de stock.
 * Contiene al menos: codigo, cantidadDevuelta y stockRestante.
 */
public class DevolucionStockResponse {

  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;

  public DevolucionStockResponse() {
  }

  public DevolucionStockResponse(String codigo, int cantidadDevuelta, int stockRestante) {
    this.codigo = codigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setCantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setStockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }
}
