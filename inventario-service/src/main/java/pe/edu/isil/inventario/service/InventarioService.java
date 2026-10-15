package pe.edu.isil.inventario.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import pe.edu.isil.inventario.domain.ProductoInventario;

/**
 * Servicio EJB responsable de las reglas de negocio del inventario.
 */
@Stateless
public class InventarioService {

  @PersistenceContext(unitName = "InventarioPU")
  private EntityManager entityManager;

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<ProductoInventario> listarProductos() {
    return entityManager
        .createQuery(
            """
            select p
            from ProductoInventario p
            order by p.id
            """,
            ProductoInventario.class
        )
        .getResultList();
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public ProductoInventario obtenerProducto(Long id) {
    if (id == null || id <= 0) {
      throw datosInvalidos("El identificador del producto es inválido.");
    }

    ProductoInventario producto = entityManager.find(ProductoInventario.class, id);
    if (producto == null) {
      throw productoNoEncontrado("No existe un producto de inventario con id " + id + ".");
    }
    return producto;
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public ProductoInventario obtenerProductoPorCodigo(String codigo) {
    if (codigo == null || codigo.isBlank()) {
      throw datosInvalidos("El código del producto es obligatorio.");
    }

    List<ProductoInventario> resultado = entityManager
        .createQuery(
            """
            select p
            from ProductoInventario p
            where p.codigo = :codigo
            """,
            ProductoInventario.class
        )
        .setParameter("codigo", codigo.trim())
        .setMaxResults(1)
        .getResultList();

    if (resultado.isEmpty()) {
      throw productoNoEncontrado("No existe un producto de inventario con código " + codigo + ".");
    }
    return resultado.getFirst();
  }

  /**
   * Reserva stock dentro de una transacción local administrada por el contenedor.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ProductoInventario reservarStock(Long productoId, int cantidad) {
    return reservar(obtenerProducto(productoId), cantidad);
  }

  /**
   * Variante recomendada para integración entre sistemas: usa un código de negocio estable.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ProductoInventario reservarStockPorCodigo(String codigo, int cantidad) {
    return reservar(obtenerProductoPorCodigo(codigo), cantidad);
  }

  private ProductoInventario reservar(ProductoInventario producto, int cantidad) {
    if (cantidad <= 0) {
      throw datosInvalidos("La cantidad debe ser mayor que cero.");
    }

    try {
      producto.reservarStock(cantidad);
      // Fuerza el UPDATE antes de abandonar el método para traducir aquí
      // un eventual conflicto de optimistic locking.
      entityManager.flush();
      return producto;
    } catch (IllegalArgumentException e) {
      throw datosInvalidos(e.getMessage());
    } catch (IllegalStateException e) {
      throw new InventarioException(
          InventarioException.Tipo.STOCK_INSUFICIENTE,
          e.getMessage()
      );
    } catch (OptimisticLockException e) {
      throw new InventarioException(
          InventarioException.Tipo.CONFLICTO_CONCURRENCIA,
          "El stock fue modificado por otra operación. Vuelva a consultar e intente nuevamente."
      );
    }
  }

  /**
   * Devuelve stock al inventario incrementando las unidades disponibles.
   * Ejecutado en una transacción local obligatoria.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ProductoInventario devolverStockPorCodigo(String codigo, int cantidad) {
    return devolver(obtenerProductoPorCodigo(codigo), cantidad);
  }

  private ProductoInventario devolver(ProductoInventario producto, int cantidad) {
    if (cantidad <= 0) {
      throw datosInvalidos("La cantidad a devolver debe ser mayor que cero.");
    }

    try {
      producto.devolverStock(cantidad);
      entityManager.flush();
      return producto;
    } catch (IllegalArgumentException e) {
      throw datosInvalidos(e.getMessage());
    } catch (OptimisticLockException e) {
      throw new InventarioException(
          InventarioException.Tipo.CONFLICTO_CONCURRENCIA,
          "El stock fue modificado por otra operación. Vuelva a consultar e intente nuevamente."
      );
    }
  }

  private static InventarioException datosInvalidos(String mensaje) {
    return new InventarioException(InventarioException.Tipo.DATOS_INVALIDOS, mensaje);
  }

  private static InventarioException productoNoEncontrado(String mensaje) {
    return new InventarioException(InventarioException.Tipo.PRODUCTO_NO_ENCONTRADO, mensaje);
  }
}
