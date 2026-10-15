package com.isil.edu.pe.spedidos.exception;

/**
 * Excepción lanzada cuando no se encuentra un pedido solicitado por ID.
 * Se mapea a HTTP 404 Not Found.
 */
public class PedidoNoEncontradoException extends RuntimeException {

  public PedidoNoEncontradoException(String message) {
    super(message);
  }
}
