# Inventario Service — Jakarta EE 11 + SQL Server Express 2025

Proyecto académico complementario de `sistema-pedidos-jsp` para la Sesión 4: **Integración de aplicativos web con componentes empresariales**.

## Stack

- Java 21
- Maven
- Jakarta EE 11
- WildFly 41
- Jakarta REST
- Jakarta Enterprise Beans (EJB)
- Jakarta Persistence 3.2
- Jakarta Transactions (JTA)
- Bean Validation
- Microsoft JDBC Driver for SQL Server 13.6
- SQL Server Express 2025

## Arquitectura

```text
Sistema de Pedidos / Postman
          |
          | HTTP + JSON
          v
ProductoResource (Jakarta REST)
          |
          | @EJB
          v
InventarioService (@Stateless)
          |
          | @PersistenceContext
          v
EntityManager / InventarioPU
          |
          | java:app/jdbc/InventarioDS
          v
SQL Server Express 2025
```

## Preparación rápida

1. Habilitar TCP/IP en SQL Server Express y configurar un puerto fijo (la guía usa 1433).
2. Habilitar autenticación SQL Server/Mixed Mode para el usuario académico.
3. Ejecutar `database/01_crear_inventario.sql` en SSMS.
4. Verificar JDK 21 y Maven.
5. Ejecutar:

```bash
mvn clean test
mvn clean wildfly:run
```

El plugin usa `-Djboss.socket.binding.port-offset=2`, por lo que WildFly escucha en HTTP **8082** y Management **9992**. Esto permite ejecutar a la vez el `sistema-pedidos` existente (HTTP 8081, Management 9990).

Abrir:

```text
http://localhost:8082/inventario-service/
```

API principal:

```text
GET  /inventario-service/api/inventario/productos
GET  /inventario-service/api/inventario/productos/1/stock
POST /inventario-service/api/inventario/productos/1/reservas
```

Body de reserva:

```json
{
  "cantidad": 2
}
```

También existen endpoints por `codigo` (`LAP-001`, `MON-001`, `TEC-001`) para mostrar un contrato menos acoplado a las claves internas de base de datos.

### Nueva API de Devolución de Stock (Implementación PA02)

- **Método / Ruta:** `POST /inventario-service/api/inventario/productos/codigo/{codigo}/devoluciones`
- **Body JSON:**
```json
{
  "cantidad": 2
}
```
- **Respuesta 200 JSON:**
```json
{
  "codigo": "LAP-001",
  "cantidadDevuelta": 2,
  "stockRestante": 10
}
```
- **Validaciones:**
  - `cantidad <= 0` retorna `400 Bad Request`.
  - Código inexistente retorna `404 Not Found`.
- **Arquitectura interna:**
  `ProductoResource` → `InventarioService (@Stateless)` → `ProductoInventario (JPA)` → SQL Server Express.
- **Pruebas unitarias:**
  Ejecutar `mvn test` para validar los casos de devolución válida e inválida en `ProductoInventarioTest`.

## Material de clase

- `docs/GUIA_IMPLEMENTACION_PASO_A_PASO.md`
- `docs/INTEGRACION_CON_SISTEMA_PEDIDOS.md`
- `requests/inventario-service.http`
- `openapi/inventario-service.yaml`
- `database/02_reset_stock_demo.sql`

## Nota académica de seguridad

`@DataSourceDefinition` contiene usuario/contraseña para reducir pasos durante el laboratorio. En producción las credenciales no deben almacenarse en código; deben externalizarse en un DataSource administrado por WildFly, Credential Store o un mecanismo de secretos. `trustServerCertificate=true` también se usa solo para el laboratorio local.
