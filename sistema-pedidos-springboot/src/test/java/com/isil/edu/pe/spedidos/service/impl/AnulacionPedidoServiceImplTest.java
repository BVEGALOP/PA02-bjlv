package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionDuplicadaException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias pedagógicas para {@link AnulacionPedidoServiceImpl}
 * utilizando JUnit 5 y Mockito, demostrando Constructor Injection y
 * desacoplamiento total sin requerir levantar el contexto de Spring ni WildFly.
 */
class AnulacionPedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private AnulacionPedidoRepository anulacionPedidoRepository;
  private DevolucionStockClient devolucionStockClient;
  private AnulacionPedidoServiceImpl anulacionPedidoService;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    anulacionPedidoRepository = mock(AnulacionPedidoRepository.class);
    devolucionStockClient = mock(DevolucionStockClient.class);

    // Inyección manual de dependencias mediante constructor
    anulacionPedidoService = new AnulacionPedidoServiceImpl(
        pedidoRepository,
        anulacionPedidoRepository,
        devolucionStockClient
    );
  }

  /**
   * PRUEBA OBLIGATORIA 1 - Anulación correcta (PA2 - Escenario 2.5 Caso 1):
   * El Service invoca una única devolución remota, marca el pedido como ANULADO
   * y guarda el registro de anulación en H2.
   */
  @Test
  void debeAnularPedidoCorrectamenteYDevolverStock() {
    // 1. Preparación (Arrange)
    Pedido pedido = new Pedido(
        1L,
        "Carlos Benitez",
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        2,
        new BigDecimal("5000.00"),
        LocalDateTime.now()
    );
    pedido.setEstado(PedidoEstado.REGISTRADO);

    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));

    when(anulacionPedidoRepository.save(any(AnulacionPedido.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // 2. Ejecución (Act)
    AnulacionPedido resultado = anulacionPedidoService.anularPedido(1L, "Solicitud del cliente");

    // 3. Verificación (Assert)
    assertNotNull(resultado, "El registro de anulación retornado no debe ser nulo.");
    assertEquals(1L, resultado.getPedidoId());
    assertEquals("LAP-001", resultado.getProductoCodigo());
    assertEquals(2, resultado.getCantidadDevuelta());
    assertEquals(PedidoEstado.ANULADO, pedido.getEstado(), "El estado del pedido debe pasar a ANULADO.");

    // Verifica que se invocó exactamente una vez la devolución remota
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    // Verifica que se guardó el pedido actualizado y la auditoría de anulación
    verify(pedidoRepository).save(pedido);
    verify(anulacionPedidoRepository).save(any(AnulacionPedido.class));
  }

  /**
   * PRUEBA OBLIGATORIA 2 - Anulación duplicada (PA2 - Escenario 2.5 Caso 2):
   * Un pedido previamente anulado se rechaza sin invocar una nueva devolución
   * de stock ni registrar una anulación adicional.
   */
  @Test
  void noDebePermitirAnulacionDuplicada() {
    // 1. Preparación (Arrange)
    Pedido pedido = new Pedido(
        1L,
        "Carlos Benitez",
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        2,
        new BigDecimal("5000.00"),
        LocalDateTime.now()
    );
    pedido.setEstado(PedidoEstado.ANULADO); // Ya se encuentra anulado previamente

    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(true);

    // 2. Ejecución y Verificación de Excepción (Act & Assert)
    AnulacionDuplicadaException excepcion = assertThrows(
        AnulacionDuplicadaException.class,
        () -> anulacionPedidoService.anularPedido(1L, "Intento duplicado")
    );

    assertNotNull(excepcion.getMessage());

    // Se verifica de forma estricta que NO se contactó a inventario ni se guardó nada
    verifyNoInteractions(devolucionStockClient);
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  /**
   * PRUEBA OBLIGATORIA 3 - Fallo de Inventario (PA2 - Escenario 2.5 Caso 4):
   * Ante una excepción del cliente remoto (servicio no disponible o timeout),
   * el Service no confirma cambios locales, el pedido conserva el estado REGISTRADO
   * y no se registra una anulación exitosa.
   */
  @Test
  void anteFalloDeInventarioNoDebeModificarPedidoNiGuardarAnulacion() {
    // 1. Preparación (Arrange)
    Pedido pedido = new Pedido(
        1L,
        "Carlos Benitez",
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        2,
        new BigDecimal("5000.00"),
        LocalDateTime.now()
    );
    pedido.setEstado(PedidoEstado.REGISTRADO);

    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);

    // Simulamos fallo técnico en el cliente remoto de inventario
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("El servicio de inventario no está disponible"));

    // 2. Ejecución y Verificación de Excepción (Act & Assert)
    assertThrows(
        InventarioNoDisponibleException.class,
        () -> anulacionPedidoService.anularPedido(1L, "Intento de anulación con inventario caído")
    );

    // El pedido debe conservar su estado intacto en REGISTRADO
    assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado(), "El pedido debe seguir como REGISTRADO.");

    // Se verifica que NO se guardaron cambios en H2
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  /**
   * PRUEBA ADICIONAL - Pedido Inexistente (PA2 - Escenario 2.5 Caso 3):
   * Intentar anular un ID que no existe en H2 arroja PedidoNoEncontradoException
   * sin interactuar con inventario.
   */
  @Test
  void debeRechazarAnulacionDePedidoInexistente() {
    when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        PedidoNoEncontradoException.class,
        () -> anulacionPedidoService.anularPedido(999L, "Pedido que no existe")
    );

    verifyNoInteractions(devolucionStockClient);
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
  }
}
