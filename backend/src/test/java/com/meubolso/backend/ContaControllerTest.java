package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.ContaRequest;
import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoConta;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ContaControllerTest {

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

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        userA = new Usuario("usera", "usera@test.com", passwordEncoder.encode("password123"), "User A");
        userA = usuarioRepository.save(userA);
        tokenA = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userA));

        userB = new Usuario("userb", "userb@test.com", passwordEncoder.encode("password123"), "User B");
        userB = usuarioRepository.save(userB);
        tokenB = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userB));
    }

    @Test
    @DisplayName("Should create account successfully for authenticated user")
    public void testCreateContaSuccess() throws Exception {
        ContaRequest request = new ContaRequest("Conta Corrente Inter", new BigDecimal("500.00"), TipoConta.CORRENTE);

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome").value("Conta Corrente Inter"))
                .andExpect(jsonPath("$.saldoInicial").value(500.00))
                .andExpect(jsonPath("$.tipo").value("CORRENTE"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating account with empty name")
    public void testCreateContaValidationError() throws Exception {
        ContaRequest request = new ContaRequest("", new BigDecimal("100.00"), TipoConta.CORRENTE);

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists());
    }

    @Test
    @DisplayName("Should return only accounts belonging to the authenticated user")
    public void testGetAllContasScopedToUser() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaRepository.save(contaA);

        Conta contaB = new Conta("Conta B", new BigDecimal("2000.00"), TipoConta.POUPANCA, userB);
        contaRepository.save(contaB);

        mockMvc.perform(get("/api/contas")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Conta A"));
    }

    @Test
    @DisplayName("Should get account by ID when owned by authenticated user")
    public void testGetContaByIdSuccess() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        mockMvc.perform(get("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contaA.getId()))
                .andExpect(jsonPath("$.nome").value("Conta A"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when requesting cross-tenant account")
    public void testGetContaByIdCrossTenant404() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        mockMvc.perform(get("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update account successfully when owned by authenticated user")
    public void testUpdateContaSuccess() throws Exception {
        Conta contaA = new Conta("Conta Antiga", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        ContaRequest updateReq = new ContaRequest("Conta Atualizada", new BigDecimal("1500.00"), TipoConta.INVESTIMENTO);

        mockMvc.perform(put("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Conta Atualizada"))
                .andExpect(jsonPath("$.saldoInicial").value(1500.00))
                .andExpect(jsonPath("$.tipo").value("INVESTIMENTO"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating cross-tenant account")
    public void testUpdateContaCrossTenant404() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        ContaRequest updateReq = new ContaRequest("Tentativa Hack", new BigDecimal("9999.00"), TipoConta.CORRENTE);

        mockMvc.perform(put("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete account when owned by authenticated user")
    public void testDeleteContaSuccess() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        mockMvc.perform(delete("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting cross-tenant account")
    public void testDeleteContaCrossTenant404() throws Exception {
        Conta contaA = new Conta("Conta A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaA = contaRepository.save(contaA);

        mockMvc.perform(delete("/api/contas/" + contaA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
