package com.meubolso.backend.dto;

import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoConta;

import java.math.BigDecimal;

public class ContaDTO {

    private Long id;
    private String nome;
    private BigDecimal saldoInicial;
    private TipoConta tipo;

    public ContaDTO() {
    }

    public ContaDTO(Long id, String nome, BigDecimal saldoInicial, TipoConta tipo) {
        this.id = id;
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
    }

    public static ContaDTO fromEntity(Conta conta) {
        if (conta == null) {
            return null;
        }
        return new ContaDTO(
                conta.getId(),
                conta.getNome(),
                conta.getSaldoInicial(),
                conta.getTipo()
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

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }
}
