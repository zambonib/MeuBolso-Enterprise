package com.meubolso.backend.dto;

import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.entity.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoDTO {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private TipoTransacao tipo;
    private Long contaId;
    private String contaNome;
    private Long categoriaId;
    private String categoriaNome;

    public TransacaoDTO() {
    }

    public TransacaoDTO(Long id, String descricao, BigDecimal valor, LocalDate data, TipoTransacao tipo,
                        Long contaId, String contaNome, Long categoriaId, String categoriaNome) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.contaId = contaId;
        this.contaNome = contaNome;
        this.categoriaId = categoriaId;
        this.categoriaNome = categoriaNome;
    }

    public static TransacaoDTO fromEntity(Transacao transacao) {
        if (transacao == null) {
            return null;
        }
        return new TransacaoDTO(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getTipo(),
                transacao.getConta() != null ? transacao.getConta().getId() : null,
                transacao.getConta() != null ? transacao.getConta().getNome() : null,
                transacao.getCategoria() != null ? transacao.getCategoria().getId() : null,
                transacao.getCategoria() != null ? transacao.getCategoria().getNome() : null
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public Long getContaId() {
        return contaId;
    }

    public void setContaId(Long contaId) {
        this.contaId = contaId;
    }

    public String getContaNome() {
        return contaNome;
    }

    public void setContaNome(String contaNome) {
        this.contaNome = contaNome;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
    }
}
