package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link AnulacionPedido}.
 */
@Repository
public interface AnulacionPedidoRepository extends JpaRepository<AnulacionPedido, Long> {

  /**
   * Consulta una anulación existente por el identificador del pedido.
   *
   * @param pedidoId identificador único del pedido
   * @return anulación encontrada, si existe
   */
  Optional<AnulacionPedido> findByPedidoId(Long pedidoId);

  /**
   * Determina si ya existe una anulación registrada para el pedido indicado.
   *
   * @param pedidoId identificador único del pedido
   * @return true si ya existe una anulación; false en caso contrario
   */
  boolean existsByPedidoId(Long pedidoId);
}
