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

    public ContaRequest() {
    }

    public ContaRequest(String nome, BigDecimal saldoInicial, TipoConta tipo) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
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
