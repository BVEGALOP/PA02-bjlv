package pe.edu.isil.inventario.api;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import pe.edu.isil.inventario.domain.ProductoInventario;
import pe.edu.isil.inventario.dto.DevolucionStockRequest;
import pe.edu.isil.inventario.dto.DevolucionStockResponse;
import pe.edu.isil.inventario.dto.ProductoResponse;
import pe.edu.isil.inventario.dto.ReservaStockRequest;
import pe.edu.isil.inventario.dto.ReservaStockResponse;
import pe.edu.isil.inventario.dto.StockResponse;
import pe.edu.isil.inventario.service.InventarioService;

/**
 * API REST responsable del catálogo y del stock.
 */
@Path("/inventario/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

  @EJB
  private InventarioService inventarioService;

  @GET
  public List<ProductoResponse> listar() {
    return inventarioService.listarProductos()
        .stream()
        .map(ProductoResource::toResponse)
        .toList();
  }

  @GET
  @Path("/{id}")
  public ProductoResponse obtener(@PathParam("id") Long id) {
    return toResponse(inventarioService.obtenerProducto(id));
  }

  @GET
  @Path("/{id}/stock")
  public StockResponse consultarStock(@PathParam("id") Long id) {
    return toStockResponse(inventarioService.obtenerProducto(id));
  }

  @POST
  @Path("/{id}/reservas")
  public Response reservar(
      @PathParam("id") Long id,
      @NotNull @Valid ReservaStockRequest request
  ) {
    ProductoInventario producto = inventarioService.reservarStock(id, request.getCantidad());
    return Response.ok(toReservaResponse(producto, request.getCantidad())).build();
  }

  @GET
  @Path("/codigo/{codigo}")
  public ProductoResponse obtenerPorCodigo(@PathParam("codigo") String codigo) {
    return toResponse(inventarioService.obtenerProductoPorCodigo(codigo));
  }

  @GET
  @Path("/codigo/{codigo}/stock")
  public StockResponse consultarStockPorCodigo(@PathParam("codigo") String codigo) {
    return toStockResponse(inventarioService.obtenerProductoPorCodigo(codigo));
  }

  @POST
  @Path("/codigo/{codigo}/reservas")
  public Response reservarPorCodigo(
      @PathParam("codigo") String codigo,
      @NotNull @Valid ReservaStockRequest request
  ) {
    ProductoInventario producto = inventarioService.reservarStockPorCodigo(
        codigo,
        request.getCantidad()
    );
    return Response.ok(toReservaResponse(producto, request.getCantidad())).build();
  }

  @POST
  @Path("/codigo/{codigo}/devoluciones")
  public Response devolverPorCodigo(
      @PathParam("codigo") String codigo,
      @NotNull @Valid DevolucionStockRequest request
  ) {
    ProductoInventario producto = inventarioService.devolverStockPorCodigo(
        codigo,
        request.getCantidad()
    );
    DevolucionStockResponse response = new DevolucionStockResponse(
        producto.getCodigo(),
        request.getCantidad(),
        producto.getStock()
    );
    return Response.ok(response).build();
  }

  private static ProductoResponse toResponse(ProductoInventario producto) {
    return new ProductoResponse(
        producto.getId(),
        producto.getCodigo(),
        producto.getNombre(),
        producto.getPrecio(),
        producto.getStock(),
        producto.getVersion()
    );
  }

  private static StockResponse toStockResponse(ProductoInventario producto) {
    return new StockResponse(
        producto.getId(),
        producto.getCodigo(),
        producto.getStock(),
        producto.getStock() > 0
    );
  }

  private static ReservaStockResponse toReservaResponse(
      ProductoInventario producto,
      int cantidadReservada
  ) {
    return new ReservaStockResponse(
        producto.getId(),
        producto.getCodigo(),
        producto.getNombre(),
        producto.getPrecio(),
        cantidadReservada,
        producto.getStock(),
        producto.getVersion()
    );
  }
}
