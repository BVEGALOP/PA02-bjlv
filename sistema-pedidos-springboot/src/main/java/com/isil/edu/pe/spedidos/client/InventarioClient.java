package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import java.util.List;

/**
 * Contrato para acceder a las operaciones proporcionadas por el
 * servicio externo de Inventario.
 *
 * <p>Esta interfaz define qué funcionalidades necesita consumir
 * el Sistema de Pedidos desde {@code inventario-service}, pero
 * no define cómo se realiza técnicamente la comunicación.</p>
 *
 * <p>La implementación concreta podría utilizar, por ejemplo,
 * HTTP/REST mediante Spring {@code RestClient}, un cliente simulado
 * para pruebas o cualquier otro mecanismo de integración.</p>
 *
 * <p>Esta separación permite que la capa de negocio dependa de
 * una abstracción en lugar de depender directamente de una
 * implementación concreta.</p>
 *
 * <p>Dentro de la arquitectura del proyecto, el flujo esperado es:</p>
 *
 * <pre>
 * PedidoService
 *      |
 *      v
 * InventarioClient
 *      |
 *      v
 * InventarioRestClient
 *      |
 *      v
 * HTTP / JSON
 *      |
 *      v
 * inventario-service
 * </pre>
 *
 * <p>Desde el punto de vista de Inyección de Dependencias,
 * {@code PedidoService} puede declarar una dependencia de tipo
 * {@code InventarioClient} y Spring puede proporcionar una
 * implementación concreta durante la creación del bean.</p>
 *
 * <p>Esto facilita:</p>
 *
 * <ul>
 *   <li>Reducir el acoplamiento entre negocio e infraestructura.</li>
 *   <li>Sustituir la implementación sin modificar {@code PedidoService}.</li>
 *   <li>Utilizar implementaciones falsas o mocks durante pruebas unitarias.</li>
 *   <li>Encapsular los detalles técnicos de la comunicación remota.</li>
 * </ul>
 *
 * @see ProductoInventarioResponse
 * @see ReservaStockResponse
 */
public interface InventarioClient {

  /**
   * Obtiene la lista de productos disponibles en el sistema
   * externo de Inventario.
   *
   * <p>Cada elemento retornado se representa mediante
   * {@link ProductoInventarioResponse}, que funciona como un DTO
   * para transportar hacia el Sistema de Pedidos la información
   * necesaria de cada producto.</p>
   *
   * <p>Una implementación basada en REST normalmente realizará
   * una solicitud HTTP equivalente conceptualmente a:</p>
   *
   * <pre>
   * GET /api/inventario/productos
   * </pre>
   *
   * <p>La interfaz no conoce ni controla detalles como la URL,
   * el puerto, los timeouts o la librería HTTP utilizada.
   * Esos aspectos corresponden a la implementación concreta.</p>
   *
   * @return lista de productos obtenidos desde el servicio de inventario;
   *         puede ser una lista vacía si no existen productos
   */
  List<ProductoInventarioResponse> listarProductos();

  /**
   * Solicita al sistema de Inventario la reserva de una determinada
   * cantidad de unidades de un producto.
   *
   * <p>El producto se identifica mediante su {@code codigo} y la
   * cantidad solicitada mediante {@code cantidad}.</p>
   *
   * <p>Una implementación REST podría traducir esta operación a una
   * solicitud HTTP similar a:</p>
   *
   * <pre>
   * POST /api/inventario/productos/codigo/{codigo}/reservas
   *
   * Body:
   * {
   *   "cantidad": 2
   * }
   * </pre>
   *
   * <p>El resultado se devuelve mediante
   * {@link ReservaStockResponse}, que puede contener información
   * como el producto reservado, precio, stock resultante u otros
   * datos definidos por el contrato del servicio.</p>
   *
   * <p>Desde la perspectiva de negocio, este método permite que
   * {@code PedidoService} solicite una reserva sin conocer los
   * detalles de HTTP ni de la implementación remota.</p>
   *
   * @param codigo código único del producto que se desea reservar
   * @param cantidad número de unidades que se desea reservar
   * @return resultado de la operación de reserva proporcionado por
   *         el servicio de Inventario
   */
  ReservaStockResponse reservarStock(String codigo, int cantidad);

}
