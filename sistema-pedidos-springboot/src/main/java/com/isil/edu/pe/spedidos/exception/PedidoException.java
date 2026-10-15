package com.isil.edu.pe.spedidos.exception;

/**
 * Excepción de negocio utilizada para representar errores ocurridos
 * durante las operaciones relacionadas con un pedido.
 *
 * <p>Esta excepción permite diferenciar los errores propios de las
 * reglas de negocio de otros errores técnicos de la aplicación.</p>
 *
 * <p>Algunos ejemplos de situaciones que pueden generar esta
 * excepción son:</p>
 *
 * <ul>
 *   <li>El cliente no fue informado.</li>
 *   <li>No se seleccionó un producto.</li>
 *   <li>La cantidad solicitada es inválida.</li>
 *   <li>El servicio de Inventario devuelve información incompleta.</li>
 *   <li>La reserva de stock no puede completarse por una condición
 *       de negocio.</li>
 * </ul>
 *
 * <p>La clase extiende {@link RuntimeException}, por lo que representa
 * una excepción no comprobada. Esto también permite que las
 * transacciones administradas por Spring realicen rollback de forma
 * predeterminada cuando la excepción se propaga desde un método
 * transaccional.</p>
 *
 * <p>Flujo conceptual:</p>
 *
 * <pre>
 * PedidoController
 *        |
 *        v
 * PedidoServiceImpl
 *        |
 *        +-- validación incorrecta
 *        |
 *        +-- error de negocio de Inventario
 *        |
 *        v
 * PedidoException
 * </pre>
 *
 * @see RuntimeException
 */
public class PedidoException extends RuntimeException {

  /**
   * Construye una excepción de negocio con un mensaje descriptivo.
   *
   * @param message descripción del error de negocio ocurrido
   */
  public PedidoException(String message) {
    super(message);
  }

  /**
   * Construye una excepción de negocio conservando además
   * la excepción original que produjo el problema.
   *
   * <p>Esta variante es útil cuando se desea traducir una excepción
   * de una capa inferior sin perder la causa original.</p>
   *
   * @param message descripción del error de negocio ocurrido
   * @param cause excepción original que produjo el error
   */
  public PedidoException(String message, Throwable cause) {
    super(message, cause);
  }
}
