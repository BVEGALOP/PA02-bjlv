package pe.edu.isil.inventario.service;

import jakarta.ejb.ApplicationException;

/**
 * Excepción de negocio. El rollback se activa si ocurre durante una operación transaccional.
 */
@ApplicationException(rollback = true)
public class InventarioException extends RuntimeException {

  public enum Tipo {
    PRODUCTO_NO_ENCONTRADO,
    STOCK_INSUFICIENTE,
    CONFLICTO_CONCURRENCIA,
    DATOS_INVALIDOS
  }

  private final Tipo tipo;

  public InventarioException(Tipo tipo, String message) {
    super(message);
    this.tipo = tipo;
  }

  public Tipo getTipo() {
    return tipo;
  }
}
