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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransacaoControllerTest {

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

    private Conta contaUserA;
    private Conta contaUserB;
    private Categoria categoriaUserA;
    private Categoria categoriaUserB;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // User A
        userA = new Usuario("usera", "usera@test.com", passwordEncoder.encode("password123"), "User A");
        userA = usuarioRepository.save(userA);
        tokenA = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userA));

        contaUserA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaUserA = contaRepository.save(contaUserA);

        categoriaUserA = new Categoria("Alimentação A", TipoTransacao.DESPESA, userA);
        categoriaUserA = categoriaRepository.save(categoriaUserA);

        // User B
        userB = new Usuario("userb", "userb@test.com", passwordEncoder.encode("password123"), "User B");
        userB = usuarioRepository.save(userB);
        tokenB = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userB));

        contaUserB = new Conta("Conta B", new BigDecimal("2000.00"), TipoConta.POUPANCA, userB);
        contaUserB = contaRepository.save(contaUserB);

        categoriaUserB = new Categoria("Salário B", TipoTransacao.RECEITA, userB);
        categoriaUserB = categoriaRepository.save(categoriaUserB);
    }

    @Test
    @DisplayName("Should create transaction and perform automatic balance adjustment")
    public void testCreateTransacaoSuccessAndBalanceUpdate() throws Exception {
        // 1. Create RECEITA transaction (+200.00)
        TransacaoRequest receitaReq = new TransacaoRequest(
                "Bonus Trabalho",
                new BigDecimal("200.00"),
                LocalDate.now(),
                TipoTransacao.RECEITA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receitaReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.descricao").value("Bonus Trabalho"))
                .andExpect(jsonPath("$.valor").value(200.00))
                .andExpect(jsonPath("$.tipo").value("RECEITA"));

        Conta updatedConta1 = contaRepository.findById(contaUserA.getId()).orElseThrow();
        assertEquals(new BigDecimal("1200.00"), updatedConta1.getSaldoInicial());

        // 2. Create DESPESA transaction (-50.00)
        TransacaoRequest despesaReq = new TransacaoRequest(
                "Lanche",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(despesaReq)))
                .andExpect(status().isCreated());

        Conta updatedConta2 = contaRepository.findById(contaUserA.getId()).orElseThrow();
        assertEquals(new BigDecimal("1150.00"), updatedConta2.getSaldoInicial());
    }

    @Test
    @DisplayName("Should update transaction and adjust account balance accurately")
    public void testUpdateTransacaoBalanceAdjustment() throws Exception {
        Transacao trans = new Transacao(
                "Supermercado",
                new BigDecimal("100.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA,
                categoriaUserA,
                userA
        );
        trans = transacaoRepository.save(trans);

        // Apply initial deduction (balance 1000 - 100 = 900)
        contaUserA.setSaldoInicial(new BigDecimal("900.00"));
        contaRepository.save(contaUserA);

        // Update transaction: change amount to 150.00 DESPESA
        TransacaoRequest updateReq = new TransacaoRequest(
                "Supermercado Grande",
                new BigDecimal("150.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(put("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(150.00))
                .andExpect(jsonPath("$.descricao").value("Supermercado Grande"));

        // Reverse 100 -> 1000, apply 150 -> 850
        Conta updatedConta = contaRepository.findById(contaUserA.getId()).orElseThrow();
        assertEquals(new BigDecimal("850.00"), updatedConta.getSaldoInicial());
    }

    @Test
    @DisplayName("Should delete transaction and reverse account balance adjustment")
    public void testDeleteTransacaoBalanceAdjustment() throws Exception {
        Transacao trans = new Transacao(
                "Venda Item",
                new BigDecimal("300.00"),
                LocalDate.now(),
                TipoTransacao.RECEITA,
                contaUserA,
                categoriaUserA,
                userA
        );
        trans = transacaoRepository.save(trans);

        // Apply initial addition (balance 1000 + 300 = 1300)
        contaUserA.setSaldoInicial(new BigDecimal("1300.00"));
        contaRepository.save(contaUserA);

        mockMvc.perform(delete("/api/transacoes/" + trans.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Balance must be reverted to 1000.00
        Conta updatedConta = contaRepository.findById(contaUserA.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), updatedConta.getSaldoInicial());
    }

    @Test
    @DisplayName("Should return 404 Not Found when User B accesses User A transaction")
    public void testMultiTenant404ProtectionOnReadUpdateDelete() throws Exception {
        Transacao transA = new Transacao(
                "Transacao User A",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA,
                categoriaUserA,
                userA
        );
        transA = transacaoRepository.save(transA);

        // GET cross-tenant
        mockMvc.perform(get("/api/transacoes/" + transA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // PUT cross-tenant
        TransacaoRequest updateReq = new TransacaoRequest(
                "Hack",
                new BigDecimal("999.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserB.getId(),
                categoriaUserB.getId()
        );
        mockMvc.perform(put("/api/transacoes/" + transA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // DELETE cross-tenant
        mockMvc.perform(delete("/api/transacoes/" + transA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should prevent cross-tenant FK injection when creating or updating transaction")
    public void testCrossTenantFKInjectionPrevention() throws Exception {
        // User A attempts to create transaction using User B's account ID -> 404
        TransacaoRequest crossAccountReq = new TransacaoRequest(
                "Injecao FK Conta",
                new BigDecimal("100.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserB.getId(), // User B's account!
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossAccountReq)))
                .andExpect(status().isNotFound());

        // User A attempts to create transaction using User B's category ID -> 404
        TransacaoRequest crossCategoriaReq = new TransacaoRequest(
                "Injecao FK Categoria",
                new BigDecimal("100.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserB.getId() // User B's category!
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossCategoriaReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter transactions by contaId, categoriaId, and tipo")
    public void testGetAllTransacoesWithFilters() throws Exception {
        Transacao t1 = new Transacao("Trans 1", new BigDecimal("10.00"), LocalDate.now(), TipoTransacao.DESPESA, contaUserA, categoriaUserA, userA);
        transacaoRepository.save(t1);

        mockMvc.perform(get("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("contaId", contaUserA.getId().toString())
                        .param("tipo", "DESPESA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Trans 1"));
    }
}
