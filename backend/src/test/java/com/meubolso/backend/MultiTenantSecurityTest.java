package com.meubolso.backend;

import com.meubolso.backend.entity.Categoria;
import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoConta;
import com.meubolso.backend.entity.TipoTransacao;
import com.meubolso.backend.entity.Transacao;
import com.meubolso.backend.entity.Usuario;
import com.meubolso.backend.repository.CategoriaRepository;
import com.meubolso.backend.repository.ContaRepository;
import com.meubolso.backend.repository.TransacaoRepository;
import com.meubolso.backend.repository.UsuarioRepository;
import com.meubolso.backend.security.SecurityUtils;
import com.meubolso.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MultiTenantSecurityTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario userA;
    private Usuario userB;
    private Conta contaUserA;
    private Conta contaUserB;
    private Categoria categoriaUserA;
    private Categoria categoriaUserB;
    private Transacao transacaoUserA;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAllInBatch();
        categoriaRepository.deleteAllInBatch();
        contaRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();

        // Create User A
        userA = new Usuario("usera", "usera@tenant.com", passwordEncoder.encode("secretA"), "Tenant User A");
        userA = usuarioRepository.save(userA);

        // Create User B
        userB = new Usuario("userb", "userb@tenant.com", passwordEncoder.encode("secretB"), "Tenant User B");
        userB = usuarioRepository.save(userB);

        // Create data for User A
        contaUserA = new Conta("Conta Corrente A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaUserA = contaRepository.save(contaUserA);

        categoriaUserA = new Categoria("Alimentacao A", TipoTransacao.DESPESA, userA);
        categoriaUserA = categoriaRepository.save(categoriaUserA);

        transacaoUserA = new Transacao("Almoco A", new BigDecimal("45.50"), LocalDate.now(), TipoTransacao.DESPESA, contaUserA, categoriaUserA, userA);
        transacaoUserA = transacaoRepository.save(transacaoUserA);

        // Create data for User B
        contaUserB = new Conta("Conta Poupanca B", new BigDecimal("5000.00"), TipoConta.POUPANCA, userB);
        contaUserB = contaRepository.save(contaUserB);

        categoriaUserB = new Categoria("Salario B", TipoTransacao.RECEITA, userB);
        categoriaUserB = categoriaRepository.save(categoriaUserB);
    }

    @Test
    @DisplayName("User A queries for Accounts should strictly return User A data")
    public void testUserAccountsIsolation() {
        List<Conta> contasA = contaRepository.findByUsuarioId(userA.getId());
        assertEquals(1, contasA.size());
        assertEquals("Conta Corrente A", contasA.get(0).getNome());
        assertEquals(userA.getId(), contasA.get(0).getUsuario().getId());

        List<Conta> contasB = contaRepository.findByUsuarioId(userB.getId());
        assertEquals(1, contasB.size());
        assertEquals("Conta Poupanca B", contasB.get(0).getNome());
        assertEquals(userB.getId(), contasB.get(0).getUsuario().getId());
    }

    @Test
    @DisplayName("User B attempting to access User A Account by ID must return Optional.empty")
    public void testCrossTenantAccountAccessDenied() {
        Optional<Conta> crossAccess = contaRepository.findByIdAndUsuarioId(contaUserA.getId(), userB.getId());
        assertTrue(crossAccess.isEmpty(), "User B should NOT be able to access User A's account");
    }

    @Test
    @DisplayName("User A queries for Categories should strictly return User A data")
    public void testUserCategoryIsolation() {
        List<Categoria> categoriasA = categoriaRepository.findByUsuarioId(userA.getId());
        assertEquals(1, categoriasA.size());
        assertEquals("Alimentacao A", categoriasA.get(0).getNome());

        Optional<Categoria> crossAccess = categoriaRepository.findByIdAndUsuarioId(categoriaUserA.getId(), userB.getId());
        assertTrue(crossAccess.isEmpty(), "User B should NOT be able to access User A's category");
    }

    @Test
    @DisplayName("User A queries for Transactions should strictly return User A data")
    public void testUserTransactionIsolation() {
        List<Transacao> transacoesA = transacaoRepository.findByUsuarioId(userA.getId());
        assertEquals(1, transacoesA.size());
        assertEquals("Almoco A", transacoesA.get(0).getDescricao());

        List<Transacao> transacoesB = transacaoRepository.findByUsuarioId(userB.getId());
        assertEquals(0, transacoesB.size());

        Optional<Transacao> crossAccess = transacaoRepository.findByIdAndUsuarioId(transacaoUserA.getId(), userB.getId());
        assertTrue(crossAccess.isEmpty(), "User B should NOT be able to access User A's transaction");
    }

    @Test
    @DisplayName("SecurityUtils should correctly retrieve authenticated UserPrincipal context")
    public void testSecurityUtilsUserContext() {
        UserPrincipal principalA = UserPrincipal.create(userA);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principalA, null, principalA.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(userA.getId(), SecurityUtils.getCurrentUserId());
        assertEquals(userA.getEmail(), SecurityUtils.getCurrentUserEmail());

        Optional<UserPrincipal> currentPrincipal = SecurityUtils.getCurrentUserPrincipal();
        assertTrue(currentPrincipal.isPresent());
        assertEquals("usera@tenant.com", currentPrincipal.get().getEmail());

        SecurityContextHolder.clearContext();
    }
}
