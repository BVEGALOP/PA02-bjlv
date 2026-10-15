package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.impl.PedidoServiceImpl;
import java.util.List;

/**
 * Define el contrato de operaciones de negocio relacionadas con los pedidos.
 *
 * <p>Esta interfaz representa la capa de servicio del Sistema de Pedidos.
 * Declara las operaciones que pueden ser utilizadas por otras capas de la
 * aplicación, principalmente por los controladores, sin exponer los detalles
 * internos de su implementación.</p>
 *
 * <p>La separación entre interfaz e implementación permite que una clase
 * consumidora dependa de una abstracción:</p>
 *
 * <pre>
 * PedidoController
 *       |
 *       v
 * PedidoService              &lt;- contrato
 *       ^
 *       |
 *       |
 * PedidoServiceImpl          &lt;- implementación
 *       |
 *       +-- PedidoRepository
 *       |
 *       +-- InventarioClient
 * </pre>
 *
 * <p>Desde la perspectiva de Inyección de Dependencias, Spring puede
 * proporcionar automáticamente una implementación de {@code PedidoService}
 * cuando una clase declare esta interfaz como dependencia.</p>
 *
 * <p>Esta separación facilita:</p>
 *
 * <ul>
 *   <li>Reducir el acoplamiento entre el Controller y la implementación.</li>
 *   <li>Sustituir implementaciones sin modificar las clases consumidoras.</li>
 *   <li>Crear mocks o stubs durante las pruebas unitarias.</li>
 *   <li>Mantener claramente definido el contrato de la capa de negocio.</li>
 * </ul>
 *
 * @see PedidoServiceImpl
 * @see Pedido
 */
public interface PedidoService {

  /**
   * Registra un nuevo pedido en el sistema.
   *
   * <p>La operación valida los datos recibidos, solicita a
   * {@code inventario-service} la reserva del stock, calcula el importe
   * total del pedido y finalmente almacena el pedido en la base de datos
   * local.</p>
   *
   * <p>Flujo conceptual:</p>
   *
   * <pre>
   * validar datos
   *      |
   *      v
   * reservar stock
   *      |
   *      v
   * calcular total
   *      |
   *      v
   * crear Pedido
   *      |
   *      v
   * guardar en H2
   * </pre>
   *
   * @param cliente nombre del cliente que realiza el pedido
   * @param productoCodigo código del producto seleccionado
   * @param cantidad número de unidades solicitadas
   * @return pedido registrado y persistido en la base de datos
   * @throws PedidoException si los datos son inválidos o la reserva
   *         de inventario no puede completarse correctamente
   */
  Pedido registrarPedido(
      String cliente,
      String productoCodigo,
      int cantidad
  );

  /**
   * Obtiene los productos disponibles desde el servicio externo
   * de Inventario.
   *
   * <p>Esta operación delega la consulta en {@code InventarioClient},
   * ocultando al Controller los detalles relacionados con HTTP,
   * JSON y la ubicación de {@code inventario-service}.</p>
   *
   * @return lista de productos obtenidos desde Inventario
   */
  List<ProductoInventarioResponse> listarProductos();

  /**
   * Obtiene los pedidos almacenados en la base de datos local.
   *
   * <p>Los pedidos son recuperados mediante la capa Repository,
   * manteniendo separados los detalles de persistencia de las
   * capas superiores de la aplicación.</p>
   *
   * @return lista de pedidos registrados
   */
  List<Pedido> listarPedidos();

}
