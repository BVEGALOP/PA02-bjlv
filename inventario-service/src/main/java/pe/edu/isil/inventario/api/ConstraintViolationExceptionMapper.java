package pe.edu.isil.inventario.api;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;
import pe.edu.isil.inventario.dto.ApiError;

@Provider
public class ConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

  @Context
  private UriInfo uriInfo;

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    String message = exception.getConstraintViolations()
        .stream()
        .map(v -> v.getMessage())
        .sorted()
        .collect(Collectors.joining(" "));

    ApiError error = new ApiError(
        Response.Status.BAD_REQUEST.getStatusCode(),
        Response.Status.BAD_REQUEST.getReasonPhrase(),
        message,
        uriInfo.getPath()
    );

    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }
}
