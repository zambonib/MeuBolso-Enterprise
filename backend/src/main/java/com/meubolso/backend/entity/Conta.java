package com.meubolso.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;

    @Column(length = 50)
    private String numeroConta;

    @Column(precision = 19, scale = 2)
    private BigDecimal chequeEspecial = BigDecimal.ZERO;

    @Column(length = 30)
    private String cor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    public Conta() {
    }

    public Conta(String nome, BigDecimal saldoInicial, TipoConta tipo, Usuario usuario) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
        this.usuario = usuario;
    }

    public Conta(String nome, BigDecimal saldoInicial, TipoConta tipo, String numeroConta, BigDecimal chequeEspecial, String cor, Usuario usuario) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
        this.numeroConta = numeroConta;
        this.chequeEspecial = chequeEspecial;
        this.cor = cor;
        this.usuario = usuario;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
