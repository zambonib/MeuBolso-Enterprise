package com.meubolso.backend.service;

import com.meubolso.backend.dto.ContaDTO;
import com.meubolso.backend.dto.ContaRequest;
import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.Usuario;
import com.meubolso.backend.exception.ResourceNotFoundException;
import com.meubolso.backend.repository.ContaRepository;
import com.meubolso.backend.repository.UsuarioRepository;
import com.meubolso.backend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public ContaService(ContaRepository contaRepository, UsuarioRepository usuarioRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ContaDTO> findAllForCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return contaRepository.findByUsuarioId(userId)
                .stream()
                .map(ContaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContaDTO findByIdForCurrentUser(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Conta conta = contaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com id: " + id));
        return ContaDTO.fromEntity(conta);
    }

    @Transactional
    public ContaDTO create(ContaRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + userId));

        Conta conta = new Conta(
                request.getNome(),
                request.getSaldoInicial(),
                request.getTipo(),
                usuario
        );

        Conta saved = contaRepository.save(conta);
        return ContaDTO.fromEntity(saved);
    }

    @Transactional
    public ContaDTO update(Long id, ContaRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Conta conta = contaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com id: " + id));

        conta.setNome(request.getNome());
        conta.setSaldoInicial(request.getSaldoInicial());
        conta.setTipo(request.getTipo());

        Conta updated = contaRepository.save(conta);
        return ContaDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Conta conta = contaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com id: " + id));

        contaRepository.delete(conta);
    }
}
