package com.isil.edu.pe.spedidos.client;

/**
 * Error controlado devuelto por inventario-service mediante HTTP 4xx/5xx.
 */
public class InventarioClientException extends RuntimeException {

  private final int status;

  public InventarioClientException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
