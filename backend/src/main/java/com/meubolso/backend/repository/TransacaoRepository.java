package com.meubolso.backend.repository;

import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuarioId(Long usuarioId);

    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    void deleteByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("SELECT t FROM Transacao t WHERE t.usuario.id = :usuarioId " +
           "AND (:contaId IS NULL OR t.conta.id = :contaId) " +
           "AND (:categoriaId IS NULL OR t.categoria.id = :categoriaId) " +
           "AND (:tipo IS NULL OR t.tipo = :tipo)")
    List<Transacao> findByUsuarioIdAndFilters(
            @Param("usuarioId") Long usuarioId,
            @Param("contaId") Long contaId,
            @Param("categoriaId") Long categoriaId,
            @Param("tipo") TipoTransacao tipo);
}
