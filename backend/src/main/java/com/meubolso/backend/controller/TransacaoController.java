package com.meubolso.backend.controller;

import com.meubolso.backend.dto.TransacaoDTO;
import com.meubolso.backend.dto.TransacaoRequest;
import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @Autowired
    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<List<TransacaoDTO>> getAll(
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) TipoTransacao tipo) {
        return ResponseEntity.ok(transacaoService.findAllForCurrentUser(contaId, categoriaId, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.findByIdForCurrentUser(id));
    }

    @PostMapping
    public ResponseEntity<TransacaoDTO> create(@Valid @RequestBody TransacaoRequest request) {
        TransacaoDTO created = transacaoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoDTO> update(@PathVariable Long id, @Valid @RequestBody TransacaoRequest request) {
        TransacaoDTO updated = transacaoService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transacaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
