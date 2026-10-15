# Siguiente etapa — consumir inventario-service desde sistema-pedidos-jsp

## Objetivo

Evolucionar el proyecto actual:

```text
Navegador -> PedidoServlet -> PedidoService -> JPA/H2
```

a:

```text
Navegador
   |
   v
PedidoServlet
   |
   v
PedidoService ---------------- HTTP/JSON ----------------> inventario-service
   |                                                         |
   v                                                         v
JPA/H2                                                  SQL Server
(pedidos/precio)                                        (stock)
```

La base H2 conserva pedidos, nombre y precio. **El stock deja de ser la fuente de verdad local** y pasa a ser responsabilidad de `inventario-service`.

## Integración mínima con el proyecto actual

Para modificar lo mínimo en una primera práctica, se puede usar el `productoId` actual porque ambos proyectos se inicializan con Laptop, Monitor y Teclado en el mismo orden.

> Esto es útil didácticamente, pero acopla dos bases de datos por sus claves internas. En una evolución posterior es preferible agregar un `codigo` de negocio (`LAP-001`, `MON-001`, `TEC-001`) al producto local y consumir los endpoints por código.

## Cliente REST sugerido

Crear en `sistema-pedidos-jsp`:

```text
src/main/java/pe/edu/isil/pedidos/integration/InventarioClient.java
```

Ejemplo con Jakarta REST Client:

```java
package pe.edu.isil.pedidos.integration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InventarioClient {

  private static final String BASE_URL =
      "http://localhost:8082/inventario-service/api/inventario/productos";

  private Client client;

  @PostConstruct
  void iniciar() {
    client = ClientBuilder.newBuilder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build();
  }

  @PreDestroy
  void cerrar() {
    if (client != null) {
      client.close();
    }
  }

  public ReservaInventarioResponse reservar(Long productoId, int cantidad) {
    ReservaInventarioRequest body = new ReservaInventarioRequest(cantidad);

    try (Response response = client
        .target(BASE_URL)
        .path(String.valueOf(productoId))
        .path("reservas")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE))) {

      if (response.getStatus() == 200) {
        return response.readEntity(ReservaInventarioResponse.class);
      }
      if (response.getStatus() == 404) {
        throw new IllegalStateException("El producto no existe en Inventario.");
      }
      if (response.getStatus() == 409) {
        throw new IllegalStateException("Stock insuficiente o conflicto de inventario.");
      }
      throw new IllegalStateException("Inventario respondió HTTP " + response.getStatus());
    } catch (ProcessingException e) {
      throw new IllegalStateException("Inventario no está disponible.", e);
    }
  }
}
```

Los DTO del cliente pueden ser POJO sencillos:

```java
public record ReservaInventarioRequest(int cantidad) {}

public record ReservaInventarioResponse(
    Long productoId,
    String codigo,
    int cantidadReservada,
    int stockRestante,
    Long version
) {}
```

## Cambio conceptual en PedidoService

Actualmente `Producto.descontarStock(cantidad)` modifica el stock local. Cuando la integración esté activa, esa responsabilidad debe salir del proyecto de pedidos.

Flujo objetivo:

```text
1. Validar cliente/producto/cantidad.
2. Obtener el producto local para precio/nombre.
3. Invocar inventario-service para reservar stock.
4. Si Inventario responde 409, abortar el pedido.
5. Si la reserva es exitosa, persistir Pedido.
```

**Importante:** la transacción JTA del sistema de pedidos NO incluye automáticamente la transacción de SQL Server del servicio remoto. Si el inventario confirma la reserva y luego falla el guardado local del pedido, puede ser necesaria una operación de compensación. Este caso se utiliza en la Sesión 4 para explicar fronteras transaccionales entre sistemas.

## Orden recomendado en clase

1. Ejecutar `inventario-service` y probarlo con el archivo `.http`.
2. Mostrar que la reserva cambia el stock en SQL Server.
3. Crear `InventarioClient` en `sistema-pedidos-jsp`.
4. Inyectarlo en `PedidoService` con `@Inject`.
5. Sustituir `producto.descontarStock(cantidad)` por la llamada remota.
6. Ejecutar ambos WildFly simultáneamente: pedidos en 8081 e inventario en 8082.
7. Detener `inventario-service` y demostrar timeout/indisponibilidad.
8. Intentar reservar más stock del disponible y observar HTTP 409.
