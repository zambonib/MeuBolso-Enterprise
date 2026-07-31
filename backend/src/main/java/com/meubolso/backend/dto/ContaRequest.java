package com.meubolso.backend.dto;

import com.meubolso.backend.entity.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ContaRequest {

    @NotBlank(message = "O nome da conta é obrigatório")
    @Size(max = 100, message = "O nome da conta deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "O saldo inicial é obrigatório")
    private BigDecimal saldoInicial;

    @NotNull(message = "O tipo da conta é obrigatório")
    private TipoConta tipo;

    private String numeroConta;
    private BigDecimal chequeEspecial;
    private String cor;

    public ContaRequest() {
    }

    public ContaRequest(String nome, BigDecimal saldoInicial, TipoConta tipo) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
    }

    public ContaRequest(String nome, BigDecimal saldoInicial, TipoConta tipo, String numeroConta, BigDecimal chequeEspecial, String cor) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
        this.numeroConta = numeroConta;
        this.chequeEspecial = chequeEspecial;
        this.cor = cor;
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
