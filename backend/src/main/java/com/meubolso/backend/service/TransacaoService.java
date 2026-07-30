package com.meubolso.backend.service;

import com.meubolso.backend.dto.TransacaoDTO;
import com.meubolso.backend.dto.TransacaoRequest;
import com.meubolso.backend.entity.Categoria;
import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.entity.Transacao;
import com.meubolso.backend.entity.Usuario;
import com.meubolso.backend.exception.ResourceNotFoundException;
import com.meubolso.backend.repository.CategoriaRepository;
import com.meubolso.backend.repository.ContaRepository;
import com.meubolso.backend.repository.TransacaoRepository;
import com.meubolso.backend.repository.UsuarioRepository;
import com.meubolso.backend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public TransacaoService(TransacaoRepository transacaoRepository,
                            ContaRepository contaRepository,
                            CategoriaRepository categoriaRepository,
                            UsuarioRepository usuarioRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> findAllForCurrentUser(Long contaId, Long categoriaId, TipoTransacao tipo) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transacaoRepository.findByUsuarioIdAndFilters(userId, contaId, categoriaId, tipo)
                .stream()
                .map(TransacaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransacaoDTO findByIdForCurrentUser(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));
        return TransacaoDTO.fromEntity(transacao);
    }

    @Transactional
    public TransacaoDTO create(TransacaoRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + userId));

        // Enforce multi-tenant FK guard for conta and categoria
        Conta conta = contaRepository.findByIdAndUsuarioId(request.getContaId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada ou não pertence ao usuário: " + request.getContaId()));

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(request.getCategoriaId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ou não pertence ao usuário: " + request.getCategoriaId()));

        Transacao transacao = new Transacao(
                request.getDescricao(),
                request.getValor(),
                request.getData(),
                request.getTipo(),
                conta,
                categoria,
                usuario
        );

        // Apply balance adjustment on account
        applyBalanceAdjustment(conta, request.getTipo(), request.getValor());
        contaRepository.save(conta);

        Transacao saved = transacaoRepository.save(transacao);
        return TransacaoDTO.fromEntity(saved);
    }

    @Transactional
    public TransacaoDTO update(Long id, TransacaoRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));

        // Multi-tenant FK guards for requested conta and categoria
        Conta newConta = contaRepository.findByIdAndUsuarioId(request.getContaId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada ou não pertence ao usuário: " + request.getContaId()));

        Categoria newCategoria = categoriaRepository.findByIdAndUsuarioId(request.getCategoriaId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ou não pertence ao usuário: " + request.getCategoriaId()));

        Conta oldConta = transacao.getConta();

        // 1. Reverse old transaction impact on old account
        reverseBalanceAdjustment(oldConta, transacao.getTipo(), transacao.getValor());

        // 2. If accounts are different, save old account, then apply new impact on new account
        if (!oldConta.getId().equals(newConta.getId())) {
            contaRepository.save(oldConta);
            applyBalanceAdjustment(newConta, request.getTipo(), request.getValor());
            contaRepository.save(newConta);
        } else {
            // Same account: apply new transaction impact on same account instance
            applyBalanceAdjustment(oldConta, request.getTipo(), request.getValor());
            contaRepository.save(oldConta);
        }

        // Update transaction fields
        transacao.setDescricao(request.getDescricao());
        transacao.setValor(request.getValor());
        transacao.setData(request.getData());
        transacao.setTipo(request.getTipo());
        transacao.setConta(newConta);
        transacao.setCategoria(newCategoria);

        Transacao updated = transacaoRepository.save(transacao);
        return TransacaoDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));

        Conta conta = transacao.getConta();

        // Reverse balance impact on account
        reverseBalanceAdjustment(conta, transacao.getTipo(), transacao.getValor());
        contaRepository.save(conta);

        transacaoRepository.delete(transacao);
    }

    private void applyBalanceAdjustment(Conta conta, TipoTransacao tipo, BigDecimal valor) {
        if (tipo == TipoTransacao.RECEITA) {
            conta.setSaldoInicial(conta.getSaldoInicial().add(valor));
        } else if (tipo == TipoTransacao.DESPESA) {
            conta.setSaldoInicial(conta.getSaldoInicial().subtract(valor));
        }
    }

    private void reverseBalanceAdjustment(Conta conta, TipoTransacao tipo, BigDecimal valor) {
        if (tipo == TipoTransacao.RECEITA) {
            conta.setSaldoInicial(conta.getSaldoInicial().subtract(valor));
        } else if (tipo == TipoTransacao.DESPESA) {
            conta.setSaldoInicial(conta.getSaldoInicial().add(valor));
        }
    }
}
