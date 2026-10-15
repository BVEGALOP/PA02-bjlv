package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.PedidoService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de las operaciones de negocio definidas por
 * {@link PedidoService}.
 *
 * <p>Esta clase contiene la lógica de negocio del Sistema de Pedidos y
 * coordina dos dependencias principales:</p>
 *
 * <ul>
 *   <li>
 *     {@link PedidoRepository}, encargado de la persistencia local
 *     de los pedidos.
 *   </li>
 *   <li>
 *     {@link InventarioClient}, encargado de comunicarse con
 *     {@code inventario-service}.
 *   </li>
 * </ul>
 *
 * <p>La anotación {@link Service @Service} indica a Spring que esta
 * clase forma parte de la capa de servicios y que debe ser registrada
 * como un bean dentro del contenedor IoC.</p>
 *
 * <p>Las dependencias se reciben mediante Constructor Injection:</p>
 *
 * <pre>
 * Spring IoC Container
 *        |
 *        +--> PedidoRepository
 *        |
 *        +--> InventarioClient
 *                 |
 *                 v
 *         PedidoServiceImpl
 * </pre>
 *
 * <p>La clase no construye directamente sus dependencias mediante
 * {@code new}. Spring se encarga de localizarlas, crearlas e
 * inyectarlas al construir este bean.</p>
 *
 * @see PedidoService
 * @see PedidoRepository
 * @see InventarioClient
 */
@Service
public class PedidoServiceImpl implements PedidoService {

  private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

  private final PedidoRepository pedidoRepository;
  private final InventarioClient inventarioClient;
  private final DevolucionStockClient devolucionStockClient;

  /**
   * Construye el servicio utilizando las dependencias proporcionadas
   * por el contenedor IoC de Spring mediante Constructor Injection.
   *
   * @param pedidoRepository repositorio utilizado para almacenar y consultar pedidos
   * @param inventarioClient cliente utilizado para comunicarse con el servicio de Inventario
   * @param devolucionStockClient cliente utilizado para compensación y devoluciones de stock
   */
  public PedidoServiceImpl(
      PedidoRepository pedidoRepository,
      InventarioClient inventarioClient,
      DevolucionStockClient devolucionStockClient) {
    this.pedidoRepository = pedidoRepository;
    this.inventarioClient = inventarioClient;
    this.devolucionStockClient = devolucionStockClient;
  }

  /**
   * Registra un pedido coordinando la lógica local con el servicio
   * externo de Inventario.
   *
   * <p>Incluye el RETO AVANZADO: Compensación distribuida. Si tras la
   * confirmación remota de la reserva ocurre un fallo local previo al commit de H2,
   * se dispara automáticamente la compensación mediante {@link DevolucionStockClient}
   * para restituir el stock en inventario-service.</p>
   */
  @Override
  @Transactional
  public Pedido registrarPedido(String cliente, String productoCodigo, int cantidad) {
    validarDatos(cliente, productoCodigo, cantidad);

    final ReservaStockResponse reserva;

    try {
      reserva = inventarioClient.reservarStock(productoCodigo.trim(), cantidad);
    } catch (InventarioClientException e) {
      if (e.getStatus() >= 500) {
        throw new InventarioNoDisponibleException(
            "El servicio de inventario presentó un error temporal (HTTP "
                + e.getStatus()
                + ").",
            e
        );
      }
      throw new PedidoException(e.getMessage(), e);
    }

    validarReserva(reserva);

    // -------------------------------------------------------------------------
    // RETO AVANZADO (+3 PUNTOS): Compensación tras fallo local previo al commit
    // -------------------------------------------------------------------------
    try {
      // Mecanismo de prueba controlado para evidenciar el fallo local:
      if ("SIMULAR_FALLO_LOCAL".equalsIgnoreCase(cliente.trim())) {
        throw new IllegalStateException("Fallo local provocado intencionalmente antes del commit en H2.");
      }

      BigDecimal total = reserva.getPrecio().multiply(BigDecimal.valueOf(cantidad));
      Pedido pedido = new Pedido(cliente.trim(), reserva.getCodigo(), reserva.getNombre(),
              reserva.getPrecio(), cantidad, total);

      return pedidoRepository.save(pedido);
    } catch (Exception localEx) {
      // Ejecución de la transacción compensatoria (Saga Pattern)
      try {
        logger.warn("Ejecutando compensación de stock en inventario-service para {} por {} unidades debido a fallo local.",
            reserva.getCodigo(), cantidad);
        devolucionStockClient.devolverStock(reserva.getCodigo(), cantidad);
        logger.info("Compensación de stock completada exitosamente.");
      } catch (Exception compEx) {
        // Si falla la propia compensación, registrar el incidente pendiente de conciliación
        logger.error("INCIDENTE CRITICO PENDIENTE DE CONCILIACION: Falló la compensación remota tras reserva confirmada.", compEx);
      }
      // Se relanza la excepción para asegurar que la transacción de Spring en H2 realice rollback completo
      throw new PedidoException("Fallo en la persistencia local de H2. Se ejecutó la compensación de stock.", localEx);
    }
  }

  /**
   * Obtiene los productos disponibles mediante el cliente de Inventario.
   *
   * <p>La implementación delega la responsabilidad de comunicación
   * externa en {@link InventarioClient}. Por ello, esta clase no conoce
   * detalles como URLs, métodos HTTP o serialización JSON.</p>
   *
   * @return lista de productos disponibles
   */
  @Override
  public List<ProductoInventarioResponse> listarProductos() {
    return inventarioClient.listarProductos();
  }

  /**
   * Obtiene todos los pedidos registrados en la base de datos local.
   *
   * <p>La consulta se ejecuta mediante {@link PedidoRepository},
   * solicitando los registros ordenados de forma descendente por
   * identificador.</p>
   *
   * <p>{@code readOnly = true} indica que la transacción se utiliza
   * únicamente para lectura y no pretende modificar información.</p>
   *
   * @return lista de pedidos ordenados por identificador descendente
   */
  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidos() {
    return pedidoRepository.findAllByOrderByIdDesc();
  }

  /**
   * Valida los datos mínimos necesarios para registrar un pedido.
   *
   * @param cliente cliente asociado al pedido
   * @param productoCodigo código del producto
   * @param cantidad cantidad solicitada
   * @throws PedidoException cuando algún dato requerido es inválido
   */
  private void validarDatos(String cliente, String productoCodigo, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException(
          "El cliente es obligatorio."
      );
    }

    if (productoCodigo == null
        || productoCodigo.isBlank()) {
      throw new PedidoException(
          "Debe seleccionar un producto."
      );
    }

    if (cantidad <= 0) {
      throw new PedidoException(
          "La cantidad debe ser mayor que cero."
      );
    }
  }

  /**
   * Valida que la respuesta retornada por Inventario contenga
   * la información mínima necesaria para crear el pedido.
   *
   * @param reserva respuesta recibida desde {@code inventario-service}
   * @throws PedidoException si la respuesta es nula o incompleta
   */
  private void validarReserva(ReservaStockResponse reserva) {
    if (reserva == null
        || reserva.getCodigo() == null
        || reserva.getCodigo().isBlank()
        || reserva.getNombre() == null
        || reserva.getNombre().isBlank()
        || reserva.getPrecio() == null) {
      throw new PedidoException(
          "inventario-service devolvió "
              + "una respuesta incompleta."
      );
    }
  }
}
