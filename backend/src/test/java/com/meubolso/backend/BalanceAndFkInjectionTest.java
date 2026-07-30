package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.TransacaoRequest;
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
import com.meubolso.backend.security.JwtTokenProvider;
import com.meubolso.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BalanceAndFkInjectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private JwtTokenProvider tokenProvider;

    private Usuario userA;
    private Usuario userB;
    private String tokenA;
    private String tokenB;

    private Conta contaA1;
    private Conta contaA2;
    private Conta contaB1;

    private Categoria categoriaA1;
    private Categoria categoriaA2;
    private Categoria categoriaB1;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // User A setup
        userA = new Usuario("usera_challenger", "usera_challenger@test.com", passwordEncoder.encode("password123"), "User A Challenger");
        userA = usuarioRepository.save(userA);
        tokenA = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userA));

        contaA1 = contaRepository.save(new Conta("Conta A1", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA));
        contaA2 = contaRepository.save(new Conta("Conta A2", new BigDecimal("500.00"), TipoConta.POUPANCA, userA));

        categoriaA1 = categoriaRepository.save(new Categoria("Salário A", TipoTransacao.RECEITA, userA));
        categoriaA2 = categoriaRepository.save(new Categoria("Alimentação A", TipoTransacao.DESPESA, userA));

        // User B setup
        userB = new Usuario("userb_challenger", "userb_challenger@test.com", passwordEncoder.encode("password123"), "User B Challenger");
        userB = usuarioRepository.save(userB);
        tokenB = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userB));

        contaB1 = contaRepository.save(new Conta("Conta B1", new BigDecimal("2000.00"), TipoConta.CORRENTE, userB));
        categoriaB1 = categoriaRepository.save(new Categoria("Investimento B", TipoTransacao.RECEITA, userB));
    }

    @Test
    @DisplayName("Balance Engine: Create RECEITA and DESPESA updates account balance correctly")
    public void testBalanceOnCreateTransactions() throws Exception {
        // Create RECEITA of 350.00 on contaA1 (1000.00 -> 1350.00)
        TransacaoRequest receitaReq = new TransacaoRequest("Projeto Freelance", new BigDecimal("350.00"), LocalDate.now(), TipoTransacao.RECEITA, contaA1.getId(), categoriaA1.getId());
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receitaReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        Conta updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1350.00"), updatedA1.getSaldoInicial());

        // Create DESPESA of 150.00 on contaA1 (1350.00 -> 1200.00)
        TransacaoRequest despesaReq = new TransacaoRequest("Mercado", new BigDecimal("150.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA1.getId(), categoriaA2.getId());
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesaReq)))
                .andExpect(status().isCreated());

        updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1200.00"), updatedA1.getSaldoInicial());
    }

    @Test
    @DisplayName("Balance Engine: Update transaction type and amount on same account")
    public void testBalanceOnUpdateTypeAndAmountSameAccount() throws Exception {
        // Create initial DESPESA transaction of 100.00 (saldo 1000.00 -> 900.00)
        Transacao trans = transacaoRepository.save(new Transacao("Restaurante", new BigDecimal("100.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA1, categoriaA2, userA));
        contaA1.setSaldoInicial(new BigDecimal("900.00"));
        contaRepository.save(contaA1);

        // Update transaction: change to RECEITA 250.00
        // Revert DESPESA 100.00 (+100.00 -> 1000.00), apply RECEITA 250.00 (+250.00 -> 1250.00)
        TransacaoRequest updateReq = new TransacaoRequest("Estorno Restaurante", new BigDecimal("250.00"), LocalDate.now(), TipoTransacao.RECEITA, contaA1.getId(), categoriaA1.getId());
        mockMvc.perform(put("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        Conta updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1250.00"), updatedA1.getSaldoInicial());
    }

    @Test
    @DisplayName("Balance Engine: Transfer transaction across accounts (Conta A1 -> Conta A2) updates both balances")
    public void testBalanceOnCrossAccountTransfer() throws Exception {
        // Transaction on Conta A1 (initial saldo 1000.00). Was RECEITA 400.00 -> current saldo 1400.00.
        Transacao trans = transacaoRepository.save(new Transacao("Transferência Origem", new BigDecimal("400.00"), LocalDate.now(), TipoTransacao.RECEITA, contaA1, categoriaA1, userA));
        contaA1.setSaldoInicial(new BigDecimal("1400.00"));
        contaRepository.save(contaA1);

        // Conta A2 initial saldo is 500.00.
        // Update transaction: move to Conta A2 with DESPESA 100.00.
        // Expected Conta A1: Revert RECEITA 400.00 (1400.00 - 400.00 = 1000.00)
        // Expected Conta A2: Apply DESPESA 100.00 (500.00 - 100.00 = 400.00)
        TransacaoRequest transferReq = new TransacaoRequest("Transferência Destino", new BigDecimal("100.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA2.getId(), categoriaA2.getId());

        mockMvc.perform(put("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk());

        Conta updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        Conta updatedA2 = contaRepository.findById(contaA2.getId()).orElseThrow();

        assertEquals(new BigDecimal("1000.00"), updatedA1.getSaldoInicial());
        assertEquals(new BigDecimal("400.00"), updatedA2.getSaldoInicial());
    }

    @Test
    @DisplayName("Balance Engine: Delete transaction reverts account balance")
    public void testBalanceOnDeleteTransaction() throws Exception {
        // Transaction on Conta A1: DESPESA 200.00 (saldo 800.00)
        Transacao trans = transacaoRepository.save(new Transacao("Compra Eletrônico", new BigDecimal("200.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA1, categoriaA2, userA));
        contaA1.setSaldoInicial(new BigDecimal("800.00"));
        contaRepository.save(contaA1);

        // Delete transaction -> expected saldo 800.00 + 200.00 = 1000.00
        mockMvc.perform(delete("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        Conta updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), updatedA1.getSaldoInicial());
    }

    @Test
    @DisplayName("Cross-Tenant FK Injection Guard: User A cannot create transaction referencing User B's Account")
    public void testCreateTransactionWithCrossTenantAccountIdBlocked() throws Exception {
        TransacaoRequest req = new TransacaoRequest("Attacking Account", new BigDecimal("100.00"), LocalDate.now(), TipoTransacao.DESPESA, contaB1.getId(), categoriaA2.getId());

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        // Ensure User B's account balance was NOT modified
        Conta updatedB1 = contaRepository.findById(contaB1.getId()).orElseThrow();
        assertEquals(new BigDecimal("2000.00"), updatedB1.getSaldoInicial());
    }

    @Test
    @DisplayName("Cross-Tenant FK Injection Guard: User A cannot create transaction referencing User B's Category")
    public void testCreateTransactionWithCrossTenantCategoryIdBlocked() throws Exception {
        TransacaoRequest req = new TransacaoRequest("Attacking Category", new BigDecimal("100.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA1.getId(), categoriaB1.getId());

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cross-Tenant FK Injection Guard: User A cannot update existing transaction to target User B's Account")
    public void testUpdateTransactionWithCrossTenantAccountIdBlocked() throws Exception {
        Transacao trans = transacaoRepository.save(new Transacao("Trans A", new BigDecimal("50.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA1, categoriaA2, userA));

        TransacaoRequest updateReq = new TransacaoRequest("Moving to User B Account", new BigDecimal("50.00"), LocalDate.now(), TipoTransacao.DESPESA, contaB1.getId(), categoriaA2.getId());

        mockMvc.perform(put("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Ensure User B's account balance was NOT modified
        Conta updatedB1 = contaRepository.findById(contaB1.getId()).orElseThrow();
        assertEquals(new BigDecimal("2000.00"), updatedB1.getSaldoInicial());
    }

    @Test
    @DisplayName("Cross-Tenant FK Injection Guard: User B cannot modify or delete User A's transaction")
    public void testCrossTenantTransactionAccessBlocked() throws Exception {
        Transacao transA = transacaoRepository.save(new Transacao("Trans A Secret", new BigDecimal("500.00"), LocalDate.now(), TipoTransacao.RECEITA, contaA1, categoriaA1, userA));

        // User B attempts PUT on User A's transaction
        TransacaoRequest hackReq = new TransacaoRequest("Hacked Trans", new BigDecimal("9999.00"), LocalDate.now(), TipoTransacao.DESPESA, contaB1.getId(), categoriaB1.getId());

        mockMvc.perform(put("/api/transacoes/" + transA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hackReq)))
                .andExpect(status().isNotFound());

        // User B attempts DELETE on User A's transaction
        mockMvc.perform(delete("/api/transacoes/" + transA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Verify transaction still exists and account balances remain unchanged
        assertTrue(transacaoRepository.findById(transA.getId()).isPresent());
        Conta updatedA1 = contaRepository.findById(contaA1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), updatedA1.getSaldoInicial());
    }
}
