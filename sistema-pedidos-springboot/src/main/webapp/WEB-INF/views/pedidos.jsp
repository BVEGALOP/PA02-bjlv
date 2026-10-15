<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <h1>Sistema de Pedidos</h1>
        </div>
    </header>

    <section class="tarjeta">
        <c:if test="${not empty error}">
            <div class="alerta error">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <c:if test="${not empty param.creado}">
            <div class="alerta exito">
                Pedido #<c:out value="${param.creado}"/> registrado correctamente.
            </div>
        </c:if>

        <h2>Registrar pedido</h2>

        <c:url var="pedidosUrl" value="/pedidos"/>

        <form method="post" action="${pedidosUrl}" class="form-grid">
            <label>
                Cliente
                <input
                        name="cliente"
                        required
                        maxlength="120"
                        placeholder="Ej. Ana Torres"
                        value="${fn:escapeXml(clienteIngresado)}">
            </label>

            <label>
                Producto
                <select name="productoCodigo" required>
                    <option value="">Seleccione un producto</option>
                    <c:forEach var="producto" items="${productos}">
                        <option
                                value="${fn:escapeXml(producto.codigo)}"
                                ${producto.stock le 0 ? 'disabled' : ''}
                                ${producto.codigo eq productoCodigoIngresado ? 'selected' : ''}>
                            <c:out value="${producto.codigo}"/> -
                            <c:out value="${producto.nombre}"/> - S/
                            <fmt:formatNumber
                                    value="${producto.precio}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                            - stock: <c:out value="${producto.stock}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>

            <label>
                Cantidad
                <input
                        name="cantidad"
                        type="number"
                        min="1"
                        value="${empty cantidadIngresada ? 1 : fn:escapeXml(cantidadIngresada)}"
                        required>
            </label>

            <button type="submit">Registrar</button>
        </form>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla">
            <h2>Pedidos registrados</h2>
            <p>
                Cada pedido conserva un snapshot del código, nombre y precio vigente al momento de la compra.
            </p>
        </div>

        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Precio unitario</th>
                    <th>Cantidad</th>
                    <th>Total</th>
                    <th>Fecha</th>
                    <th>Estado</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.productoCodigo}"/></td>
                        <td><c:out value="${pedido.productoNombre}"/></td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.precioUnitario}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${pedido.cantidad}"/></td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.total}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${pedido.fecha}"/></td>
                        <td>
                            <strong><c:out value="${pedido.estado}"/></strong>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty pedidos}">
                    <tr>
                        <td colspan="9" class="sin-datos">Aún no hay pedidos.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</body>
</html>
