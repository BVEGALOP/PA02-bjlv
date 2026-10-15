package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Pedido}.
 *
 * <p>Esta interfaz representa la capa de persistencia del
 * Sistema de Pedidos. Su responsabilidad es proporcionar
 * operaciones para consultar y almacenar objetos
 * {@code Pedido} sin que la capa de negocio tenga que
 * trabajar directamente con {@code EntityManager},
 * JPQL o SQL.</p>
 *
 * <p>Al extender de {@link JpaRepository}, Spring Data JPA
 * genera automáticamente una implementación en tiempo de
 * ejecución. Por este motivo, no es necesario crear una clase
 * concreta como {@code PedidoRepositoryImpl} para las
 * operaciones CRUD habituales.</p>
 *
 * <p>De forma conceptual, este repositorio permite realizar
 * operaciones como:</p>
 *
 * <ul>
 *   <li>Registrar un pedido.</li>
 *   <li>Buscar un pedido por su identificador.</li>
 *   <li>Listar todos los pedidos.</li>
 *   <li>Eliminar un pedido.</li>
 *   <li>Definir consultas derivadas mediante el nombre
 *       de los métodos.</li>
 * </ul>
 *
 * <p>Dentro de la arquitectura de la aplicación, el flujo
 * esperado es:</p>
 *
 * <pre>
 * PedidoController
 *        |
 *        v
 * PedidoService
 *        |
 *        v
 * PedidoRepository
 *        |
 *        v
 * Spring Data JPA / Hibernate
 *        |
 *        v
 * Base de datos H2
 * </pre>
 *
 * <p>Esta separación ayuda a mantener desacoplada la lógica
 * de negocio de los detalles técnicos de persistencia.</p>
 *
 * @author Carlos.Nole
 * @see Pedido
 * @see JpaRepository
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  /**
   * Obtiene todos los pedidos registrados y los ordena
   * de forma descendente por su identificador.
   *
   * <p>Spring Data JPA interpreta automáticamente el nombre
   * del método y genera la consulta necesaria, por lo que no
   * es necesario escribir JPQL ni SQL de forma explícita.</p>
   *
   * <p>La expresión {@code OrderByIdDesc} indica que los
   * resultados deben ordenarse por el atributo {@code id}
   * desde el valor más alto al más bajo. En este caso,
   * normalmente permite mostrar primero los pedidos más
   * recientes.</p>
   *
   * <p>Equivalencia conceptual aproximada:</p>
   *
   * <pre>
   * SELECT p
   * FROM Pedido p
   * ORDER BY p.id DESC
   * </pre>
   *
   * @return lista de pedidos ordenada por {@code id}
   *         en orden descendente
   */
  List<Pedido> findAllByOrderByIdDesc();

}
