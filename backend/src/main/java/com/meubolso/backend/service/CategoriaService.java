package com.meubolso.backend.service;

import com.meubolso.backend.dto.CategoriaDTO;
import com.meubolso.backend.dto.CategoriaRequest;
import com.meubolso.backend.entity.Categoria;
import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.entity.Usuario;
import com.meubolso.backend.exception.ResourceNotFoundException;
import com.meubolso.backend.repository.CategoriaRepository;
import com.meubolso.backend.repository.UsuarioRepository;
import com.meubolso.backend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> findAllForCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return categoriaRepository.findByUsuarioId(userId)
                .stream()
                .map(CategoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO findByIdForCurrentUser(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));
        return CategoriaDTO.fromEntity(categoria);
    }

    @Transactional
    public CategoriaDTO create(CategoriaRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + userId));

        Categoria categoria = new Categoria(
                request.getNome(),
                request.getTipo(),
                usuario
        );

        Categoria saved = categoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(saved);
    }

    @Transactional
    public CategoriaDTO update(Long id, CategoriaRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));

        categoria.setNome(request.getNome());
        categoria.setTipo(request.getTipo());

        Categoria updated = categoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));

        categoriaRepository.delete(categoria);
    }

    @Transactional
    public void createDefaultCategoriesForUser(Usuario user) {
        List<Categoria> defaults = List.of(
                new Categoria("Salário", TipoTransacao.RECEITA, user),
                new Categoria("Investimentos", TipoTransacao.RECEITA, user),
                new Categoria("Outras Receitas", TipoTransacao.RECEITA, user),
                new Categoria("Alimentação", TipoTransacao.DESPESA, user),
                new Categoria("Moradia", TipoTransacao.DESPESA, user),
                new Categoria("Transporte", TipoTransacao.DESPESA, user),
                new Categoria("Lazer", TipoTransacao.DESPESA, user),
                new Categoria("Saúde", TipoTransacao.DESPESA, user),
                new Categoria("Educação", TipoTransacao.DESPESA, user),
                new Categoria("Outras Despesas", TipoTransacao.DESPESA, user)
        );
        categoriaRepository.saveAll(defaults);
    }
}
