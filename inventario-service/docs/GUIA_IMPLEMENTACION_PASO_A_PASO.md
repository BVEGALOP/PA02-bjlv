# Guía paso a paso — inventario-service

## 1. Objetivo

Construir un segundo aplicativo empresarial que represente el **sistema externo de inventario** de la Sesión 4.

Arquitectura:

```text
Sistema de Pedidos / Postman
          |
          | HTTP + JSON
          v
   ProductoResource
     Jakarta REST
          |
          | @EJB
          v
   InventarioService
      @Stateless
          |
          | @PersistenceContext
          v
   EntityManager / JPA
          |
          | DataSource / JDBC
          v
 SQL Server Express 2025
```

## 2. Stack tecnológico

Se conserva el stack del proyecto base:

- Java 21
- Maven
- Jakarta EE 11
- WildFly 41
- Jakarta REST
- Jakarta Enterprise Beans
- Jakarta Persistence 3.2
- Jakarta Transactions
- Bean Validation

La diferencia es la persistencia:

- proyecto `sistema-pedidos`: H2 / `ExampleDS`
- proyecto `inventario-service`: SQL Server Express 2025 / `InventarioDS`

## 3. Configurar SQL Server Express 2025

### 3.1 Abrir SQL Server Configuration Manager

En Windows:

```text
C:\Windows\SysWOW64\SQLServerManager17.msc
```

### 3.2 Habilitar TCP/IP

1. Abrir `SQL Server Network Configuration`.
2. Entrar en `Protocols for SQLEXPRESS`.
3. Habilitar `TCP/IP`.
4. Abrir las propiedades de TCP/IP.
5. En `IP Addresses > IPAll`:
   - dejar `TCP Dynamic Ports` vacío;
   - configurar `TCP Port = 1433`.
6. Reiniciar el servicio `SQL Server (SQLEXPRESS)`.

Al usar un puerto fijo y colocarlo explícitamente en el URL JDBC no dependemos del servicio SQL Server Browser para descubrir el puerto de la instancia.

> Si 1433 ya está utilizado, elija otro puerto y modifique el URL JDBC en `RestApplication.java`.

### 3.3 Habilitar autenticación SQL Server

Para esta práctica se usa un login SQL (`inventario_app`). La instancia debe aceptar autenticación SQL Server (Mixed Mode). Si solo utiliza autenticación Windows, active Mixed Mode y reinicie el servicio.

## 4. Crear base de datos, login, tabla y datos

Abrir SQL Server Management Studio y ejecutar:

```text
database/01_crear_inventario.sql
```

Se crea:

```text
InventarioDB
  └── dbo.producto_inventario
```

Usuario académico:

```text
inventario_app
Inventario2026!
```

## 5. Revisar el `pom.xml`

Se mantiene Jakarta EE 11 y WildFly 41. Se agrega el driver oficial de Microsoft:

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>13.6.0.jre11</version>
</dependency>
```

El artefacto `jre11` es compatible con Java 11 o superior, incluido Java 21.

El servicio usa `-Djboss.socket.binding.port-offset=2`: HTTP queda en **8082** y Management en **9992**. Esto evita que dos instancias de WildFly choquen en puertos internos cuando `sistema-pedidos` se ejecuta al mismo tiempo en 8081/9990.

## 6. Definir el DataSource

Archivo:

```text
src/main/java/pe/edu/isil/inventario/api/RestApplication.java
```

La anotación `@DataSourceDefinition` registra:

```text
java:app/jdbc/InventarioDS
```

con el URL:

```text
jdbc:sqlserver://localhost:1433;
databaseName=InventarioDB;
encrypt=true;
trustServerCertificate=true
```

`trustServerCertificate=true` se usa solo para simplificar el laboratorio local. En un entorno real debe validarse correctamente el certificado del servidor.

## 7. Configurar Jakarta Persistence

Archivo:

```text
src/main/resources/META-INF/persistence.xml
```

La unidad `InventarioPU` usa JTA y el DataSource:

```xml
<jta-data-source>java:app/jdbc/InventarioDS</jta-data-source>
```

El esquema no se recrea automáticamente porque lo administra el script SQL:

```xml
<property
    name="jakarta.persistence.schema-generation.database.action"
    value="none"/>
```

## 8. Crear la entidad

`ProductoInventario` representa una fila de `producto_inventario`.

La propiedad:

```java
@Version
private Long version;
```

activa **optimistic locking**. Si dos operaciones intentan modificar la misma versión del stock concurrentemente, Jakarta Persistence puede detectar el conflicto.

## 9. Implementar la lógica de negocio

`InventarioService` es un EJB `@Stateless`.

Las consultas usan:

```java
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
```

La reserva de stock usa:

```java
@TransactionAttribute(TransactionAttributeType.REQUIRED)
```

Por tanto, la actualización se ejecuta dentro de una transacción administrada por el contenedor.

## 10. Exponer la API REST

Base REST:

```text
/api
```

Recurso:

```text
/api/inventario/productos
```

Endpoints:

| Método | Endpoint | Objetivo |
|---|---|---|
| GET | `/api/inventario/productos` | listar productos |
| GET | `/api/inventario/productos/{id}` | consultar producto |
| GET | `/api/inventario/productos/{id}/stock` | consultar stock |
| POST | `/api/inventario/productos/{id}/reservas` | descontar/reservar stock |
| GET | `/api/inventario/productos/codigo/{codigo}/stock` | consultar por código estable |
| POST | `/api/inventario/productos/codigo/{codigo}/reservas` | reservar por código estable |

## 11. Manejar errores HTTP

`InventarioExceptionMapper` convierte reglas de negocio en respuestas HTTP:

- 400: datos inválidos
- 404: producto inexistente
- 409: stock insuficiente o conflicto de concurrencia

Bean Validation también devuelve 400 cuando la cantidad no es válida.

## 12. Compilar y ejecutar

Requisitos:

```bash
java -version
mvn -version
```

Desde la raíz:

```bash
mvn clean test
mvn clean wildfly:run
```

WildFly se provisiona en `target/server` y escucha en:

```text
http://localhost:8082
```

Abrir:

```text
http://localhost:8082/inventario-service/
```

## 13. Probar con Postman, curl o archivo `.http`

### Listar

```bash
curl http://localhost:8082/inventario-service/api/inventario/productos
```

### Consultar stock

```bash
curl http://localhost:8082/inventario-service/api/inventario/productos/1/stock
```

### Reservar stock

```bash
curl -X POST \
  http://localhost:8082/inventario-service/api/inventario/productos/1/reservas \
  -H "Content-Type: application/json" \
  -d '{"cantidad":2}'
```

También puede usarse:

```text
requests/inventario-service.http
```

## 14. Resultado esperado

Una reserva exitosa devuelve HTTP `200 OK` y un JSON similar a:

```json
{
  "productoId": 1,
  "codigo": "LAP-001",
  "cantidadReservada": 2,
  "stockRestante": 8,
  "version": 1
}
```

Una reserva mayor al stock devuelve HTTP `409 Conflict`.

## 15. Cómo se integrará con `sistema-pedidos`

La siguiente evolución de `sistema-pedidos` será crear un cliente REST que invoque:

```text
GET  http://localhost:8082/inventario-service/api/inventario/productos/{id}/stock
POST http://localhost:8082/inventario-service/api/inventario/productos/{id}/reservas
```

El flujo quedará:

```text
Navegador
   |
   v
PedidoServlet
   |
   v
PedidoService
   |
   +---- HTTP/JSON ----> inventario-service
                           |
                           v
                     InventarioService
                           |
                           v
                     SQL Server 2025
```

Con ello se demuestra la **integración inter-sistemas** de la Sesión 4.

## 16. Nota de seguridad

La credencial de SQL Server está escrita en código exclusivamente para facilitar la demostración académica. En producción debe moverse a un DataSource administrado por WildFly y protegerse con Credential Store/secretos de entorno.


## 17. Contrato OpenAPI

El archivo `openapi/inventario-service.yaml` documenta el contrato principal de la API. Se incluye como material de clase para relacionar la implementación con el concepto de **contrato de integración** de la Sesión 4.

## 18. Integración con sistema-pedidos

Ver `docs/INTEGRACION_CON_SISTEMA_PEDIDOS.md`. La primera integración puede realizarse por `productoId` para modificar lo mínimo; posteriormente se recomienda utilizar `codigo` como identificador de negocio estable.
