package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias pedagógicas de {@link PedidoServiceImpl}.
 *
 * <p>Estas pruebas muestran una ventaja de Constructor Injection: el servicio
 * puede construirse directamente con dependencias simuladas, sin levantar el
 * ApplicationContext de Spring ni ejecutar {@code inventario-service}.</p>
 *
 * <pre>
 * PedidoRepository mock ------\
 *                             |
 * InventarioClient mock ------+--> new PedidoServiceImpl(...)
 *                             |
 * DevolucionStockClient mock -/
 * </pre>
 */
class PedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private InventarioClient inventarioClient;
  private DevolucionStockClient devolucionStockClient;
  private PedidoServiceImpl pedidoService;

  /**
   * Crea mocks de las dependencias y construye manualmente el servicio antes
   * de cada prueba. Spring no participa en esta inicialización.
   */
  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    inventarioClient = mock(InventarioClient.class);
    devolucionStockClient = mock(DevolucionStockClient.class);

    pedidoService = new PedidoServiceImpl(
        pedidoRepository,
        inventarioClient,
        devolucionStockClient
    );
  }

  /**
   * Verifica el flujo exitoso: Inventario devuelve precio y producto, el
   * servicio calcula el total y delega la persistencia al Repository.
   */
  @Test
  void debeRegistrarPedidoYCalcularTotal() {
    ReservaStockResponse reserva = nuevaReserva(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00")
    );

    when(inventarioClient.reservarStock("LAP-001", 2))
        .thenReturn(reserva);

    when(pedidoRepository.save(any(Pedido.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Pedido pedido = pedidoService.registrarPedido(
        "Ana Torres",
        "LAP-001",
        2
    );

    assertEquals("Ana Torres", pedido.getCliente());
    assertEquals("LAP-001", pedido.getProductoCodigo());
    assertEquals(new BigDecimal("2500.00"), pedido.getPrecioUnitario());
    assertEquals(new BigDecimal("5000.00"), pedido.getTotal());

    verify(inventarioClient).reservarStock("LAP-001", 2);
    verify(pedidoRepository).save(any(Pedido.class));
  }

  /**
   * Verifica que una cantidad inválida sea rechazada antes de invocar las
   * dependencias externas o la persistencia.
   */
  @Test
  void debeRechazarCantidadMenorOIgualACero() {
    PedidoException error = assertThrows(
        PedidoException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 0)
    );

    assertEquals("La cantidad debe ser mayor que cero.", error.getMessage());
    verifyNoInteractions(inventarioClient, pedidoRepository);
  }

  /**
   * Verifica que un error funcional HTTP 4xx se traduzca a PedidoException y
   * conserve la excepción original como causa para facilitar el diagnóstico.
   */
  @Test
  void debeConservarCausaDelErrorFuncionalDeInventario() {
    InventarioClientException causa = new InventarioClientException(
        409,
        "Stock insuficiente."
    );

    when(inventarioClient.reservarStock("LAP-001", 5))
        .thenThrow(causa);

    PedidoException error = assertThrows(
        PedidoException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 5)
    );

    assertEquals("Stock insuficiente.", error.getMessage());
    assertSame(causa, error.getCause());
    verifyNoInteractions(pedidoRepository);
  }

  /**
   * Verifica que un HTTP 5xx remoto sea tratado como indisponibilidad técnica
   * y no como un error de datos ingresados por el usuario.
   */
  @Test
  void debeTratarError5xxComoServicioNoDisponible() {
    InventarioClientException causa = new InventarioClientException(
        500,
        "Error interno de inventario."
    );

    when(inventarioClient.reservarStock("LAP-001", 1))
        .thenThrow(causa);

    InventarioNoDisponibleException error = assertThrows(
        InventarioNoDisponibleException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 1)
    );

    assertSame(causa, error.getCause());
    verifyNoInteractions(pedidoRepository);
  }

  /**
   * PRUEBA RETO AVANZADO (+3 PUNTOS): Compensación tras fallo local previo al commit.
   *
   * Escenario: Tras una reserva confirmada en Inventario (descuento de 2 unidades),
   * ocurre un fallo local antes del commit en H2.
   * Se verifica que la compensación invoca a DevolucionStockClient.devolverStock(...)
   * para restituir las 2 unidades remotas y que ningún pedido queda confirmado en H2.
   */
  @Test
  void debeEjecutarTransaccionCompensatoriaTrasFalloLocalPrevioAlCommit() {
    // 1. Preparación (Arrange)
    ReservaStockResponse reserva = nuevaReserva(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00")
    );

    // Reserva confirmada por inventario remoto
    when(inventarioClient.reservarStock("LAP-001", 2))
        .thenReturn(reserva);

    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));

    // 2. Ejecución (Act) provocando fallo local controlado
    assertThrows(
        PedidoException.class,
        () -> pedidoService.registrarPedido("SIMULAR_FALLO_LOCAL", "LAP-001", 2)
    );

    // 3. Verificación (Assert)
    // Se confirma que se solicitó la reserva remota inicial
    verify(inventarioClient).reservarStock("LAP-001", 2);
    // Se confirma que la compensación REST se disparó inmediatamente para devolver el stock
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    // Se confirma que ningún pedido fue persistido exitosamente en el repositorio de H2
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  private ReservaStockResponse nuevaReserva(
      String codigo,
      String nombre,
      BigDecimal precio
  ) {
    ReservaStockResponse reserva = new ReservaStockResponse();
    reserva.setCodigo(codigo);
    reserva.setNombre(nombre);
    reserva.setPrecio(precio);
    return reserva;
  }
}