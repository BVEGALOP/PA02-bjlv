package pe.edu.isil.inventario.api;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.isil.inventario.dto.ApiError;
import pe.edu.isil.inventario.service.InventarioException;

@Provider
public class InventarioExceptionMapper implements ExceptionMapper<InventarioException> {

  @Context
  private UriInfo uriInfo;

  @Override
  public Response toResponse(InventarioException exception) {
    int status = switch (exception.getTipo()) {
      case PRODUCTO_NO_ENCONTRADO -> Response.Status.NOT_FOUND.getStatusCode();
      case STOCK_INSUFICIENTE, CONFLICTO_CONCURRENCIA -> Response.Status.CONFLICT.getStatusCode();
      case DATOS_INVALIDOS -> Response.Status.BAD_REQUEST.getStatusCode();
    };

    ApiError error = new ApiError(
        status,
        Response.Status.fromStatusCode(status).getReasonPhrase(),
        exception.getMessage(),
        uriInfo.getPath()
    );

    return Response.status(status).entity(error).build();
  }
}
