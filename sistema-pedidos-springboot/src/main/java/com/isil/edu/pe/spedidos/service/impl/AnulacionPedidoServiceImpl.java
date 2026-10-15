package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionDuplicadaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link AnulacionPedidoService} que coordina la anulación
 * de pedidos mediante Constructor Injection.
 *
 * <p>Flujo arquitectónico:</p>
 * <pre>
 * AnulacionPedidoController
 *           |
 *           v
 * AnulacionPedidoServiceImpl
 *    +------+------+
 *    |             |
 *    v             v
 * H2 (Spring Data) DevolucionStockClient
 *                      |
 *                      v (HTTP POST)
 *               inventario-service (WildFly / SQL Server)
 * </pre>
 */
@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

  private final PedidoRepository pedidoRepository;
  private final AnulacionPedidoRepository anulacionPedidoRepository;
  private final DevolucionStockClient devolucionStockClient;

  /**
   * Constructor Injection de las dependencias requeridas.
   *
   * @param pedidoRepository repositorio de pedidos locales en H2
   * @param anulacionPedidoRepository repositorio de anulaciones locales en H2
   * @param devolucionStockClient cliente HTTP desacoplado hacia inventario-service
   */
  public AnulacionPedidoServiceImpl(
      PedidoRepository pedidoRepository,
      AnulacionPedidoRepository anulacionPedidoRepository,
      DevolucionStockClient devolucionStockClient) {
    this.pedidoRepository = pedidoRepository;
    this.anulacionPedidoRepository = anulacionPedidoRepository;
    this.devolucionStockClient = devolucionStockClient;
  }

  @Override
  @Transactional
  public AnulacionPedido anularPedido(Long pedidoId, String motivo) {
    if (pedidoId == null || pedidoId <= 0) {
      throw new PedidoException("El identificador del pedido es obligatorio y debe ser mayor que cero.");
    }

    // 1. Validar existencia del pedido en la base de datos local
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PedidoNoEncontradoException("No existe un pedido con identificador " + pedidoId + "."));

    // 2. Validar que no haya sido anulado previamente
    if (pedido.getEstado() == PedidoEstado.ANULADO || anulacionPedidoRepository.existsByPedidoId(pedidoId)) {
      throw new AnulacionDuplicadaException("El pedido con identificador " + pedidoId + " ya fue anulado anteriormente.");
    }

    // 3. Solicitar restitución de stock a inventario-service (WildFly / SQL Server)
    // Si la llamada remota falla (ej. 4xx, 5xx o timeout), se propaga la excepción
    // y la transacción local en H2 no confirma cambios.
    devolucionStockClient.devolverStock(pedido.getProductoCodigo(), pedido.getCantidad());

    // 4. Actualizar el estado del pedido a ANULADO (sin eliminarlo físicamente)
    pedido.setEstado(PedidoEstado.ANULADO);
    pedidoRepository.save(pedido);

    // 5. Registrar la anulación para auditoría y trazabilidad
    AnulacionPedido anulacion = new AnulacionPedido(
        pedido.getId(),
        pedido.getProductoCodigo(),
        pedido.getCantidad(),
        motivo != null && !motivo.isBlank() ? motivo.trim() : "Sin motivo especificado"
    );

    return anulacionPedidoRepository.save(anulacion);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnulacionPedido> listarAnulaciones() {
    return anulacionPedidoRepository.findAll();
  }
}
