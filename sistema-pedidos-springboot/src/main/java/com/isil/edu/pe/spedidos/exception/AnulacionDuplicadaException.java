package com.isil.edu.pe.spedidos.exception;

/**
 * Excepción lanzada cuando se intenta anular un pedido que ya fue anulado previamente.
 * Se mapea a HTTP 409 Conflict.
 */
public class AnulacionDuplicadaException extends RuntimeException {

  public AnulacionDuplicadaException(String message) {
    super(message);
  }
}
