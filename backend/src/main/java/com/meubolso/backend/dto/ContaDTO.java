package com.meubolso.backend.dto;

import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoConta;

import java.math.BigDecimal;

public class ContaDTO {

    private Long id;
    private String nome;
    private BigDecimal saldoInicial;
    private TipoConta tipo;
    private String numeroConta;
    private BigDecimal chequeEspecial;
    private String cor;

    public ContaDTO() {
    }

    public ContaDTO(Long id, String nome, BigDecimal saldoInicial, TipoConta tipo) {
        this.id = id;
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
    }

    public ContaDTO(Long id, String nome, BigDecimal saldoInicial, TipoConta tipo, String numeroConta, BigDecimal chequeEspecial, String cor) {
        this.id = id;
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
        this.numeroConta = numeroConta;
        this.chequeEspecial = chequeEspecial;
        this.cor = cor;
    }

    public static ContaDTO fromEntity(Conta conta) {
        if (conta == null) {
            return null;
        }
        return new ContaDTO(
                conta.getId(),
                conta.getNome(),
                conta.getSaldoInicial(),
                conta.getTipo(),
                conta.getNumeroConta(),
                conta.getChequeEspecial(),
                conta.getCor()
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

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public BigDecimal getChequeEspecial() {
        return chequeEspecial;
    }

    public void setChequeEspecial(BigDecimal chequeEspecial) {
        this.chequeEspecial = chequeEspecial;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }
}
