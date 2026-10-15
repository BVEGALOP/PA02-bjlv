package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.ApiError;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Implementación de {@link DevolucionStockClient} que se comunica con
 * {@code inventario-service} mediante HTTP/REST utilizando Spring {@link RestClient}.
 */
@Component
public class DevolucionStockRestClient implements DevolucionStockClient {

  private final RestClient restClient;
  private final String baseUrl;

  /**
   * Constructor injection de dependencias gestionadas por Spring IoC.
   *
   * @param restClient cliente HTTP preconfigurado con timeouts
   * @param baseUrl URL base centralizada en application.properties
   */
  public DevolucionStockRestClient(
      @Qualifier("inventarioHttpClient") RestClient restClient,
      @Value("${inventario.api.base-url}") String baseUrl) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  @Override
  public DevolucionStockResponse devolverStock(String codigo, int cantidad) {
    DevolucionStockRequest request = new DevolucionStockRequest(cantidad);
    try {
      return restClient
          .post()
          .uri(baseUrl + "/productos/codigo/{codigo}/devoluciones", codigo)
          .body(request)
          .retrieve()
          .body(DevolucionStockResponse.class);
    } catch (RestClientResponseException e) {
      throw convertirError(e);
    } catch (ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  private InventarioClientException convertirError(RestClientResponseException e) {
    String mensaje = "inventario-service respondió HTTP " + e.getStatusCode().value() + ".";

    try {
      ApiError error = e.getResponseBodyAs(ApiError.class);
      if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
        mensaje = error.getMessage();
      }
    } catch (RuntimeException ignored) {
      // Si no tiene estructura ApiError, mantiene el mensaje por defecto
    }

    return new InventarioClientException(e.getStatusCode().value(), mensaje);
  }

  private InventarioNoDisponibleException noDisponible(ResourceAccessException cause) {
    return new InventarioNoDisponibleException(
        "El servicio de inventario no se encuentra disponible o excedió el tiempo de espera.",
        cause
    );
  }
}
