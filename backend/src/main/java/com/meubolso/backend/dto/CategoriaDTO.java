package com.meubolso.backend.dto;

import com.meubolso.backend.entity.Categoria;
import com.meubolso.backend.entity.TipoTransacao;

public class CategoriaDTO {

    private Long id;
    private String nome;
    private TipoTransacao tipo;

    public CategoriaDTO() {
    }

    public CategoriaDTO(Long id, String nome, TipoTransacao tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
    }

    public static CategoriaDTO fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }
}
