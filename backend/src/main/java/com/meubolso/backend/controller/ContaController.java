package com.meubolso.backend.controller;

import com.meubolso.backend.dto.ContaDTO;
import com.meubolso.backend.dto.ContaRequest;
import com.meubolso.backend.service.ContaService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;

    @Autowired
    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public ResponseEntity<List<ContaDTO>> getAll() {
        return ResponseEntity.ok(contaService.findAllForCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.findByIdForCurrentUser(id));
    }

    @PostMapping
    public ResponseEntity<ContaDTO> create(@Valid @RequestBody ContaRequest request) {
        ContaDTO created = contaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaDTO> update(@PathVariable Long id, @Valid @RequestBody ContaRequest request) {
        ContaDTO updated = contaService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
