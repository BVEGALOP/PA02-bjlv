package com.isil.edu.pe.spedidos.client;

/**
 * Excepción técnica que representa una indisponibilidad de {@code inventario-service}.
 *
 * <p>Puede utilizarse cuando no es posible establecer la comunicación, cuando se
 * supera un timeout o cuando el servicio remoto responde con un error HTTP 5xx
 * que impide completar temporalmente la operación solicitada.</p>
 *
 * <p>Se mantiene separada de las excepciones de negocio para que la capa web pueda
 * responder con un estado HTTP apropiado, por ejemplo {@code 503 Service Unavailable},
 * en lugar de presentar el problema como un error de datos del usuario.</p>
 */
public class InventarioNoDisponibleException extends RuntimeException {

  public InventarioNoDisponibleException(String message) {
    super(message);
  }

  public InventarioNoDisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
