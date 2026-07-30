package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.CategoriaDTO;
import com.meubolso.backend.dto.ContaRequest;
import com.meubolso.backend.dto.LoginRequest;
import com.meubolso.backend.dto.RegisterRequest;
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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ComprehensiveIntegrationAndMultiTenantTestSuite {

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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // =========================================================================
    // REQUIREMENT 1: User Registration & Default Categories Auto-Creation
    // =========================================================================
    @Nested
    @DisplayName("1. Registration & Default Categories Suite")
    class UserRegistrationAndCategorySeedingTests {

        @Test
        @DisplayName("REQ-1.1: Register new user seeds 10 default categories (3 RECEITA, 7 DESPESA)")
        public void testUserRegistrationSeedsDefaultCategories() throws Exception {
            RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "securePass123", "John Doe");

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.user.email").value("john@example.com"))
                    .andReturn();

            String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

            // Verify via REST API
            mockMvc.perform(get("/api/categorias")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(10)));

            // Verify directly in DB
            Usuario user = usuarioRepository.findByEmail("john@example.com").orElseThrow();
            List<Categoria> categories = categoriaRepository.findByUsuarioId(user.getId());
            assertEquals(10, categories.size());

            long receitaCount = categories.stream().filter(c -> c.getTipo() == TipoTransacao.RECEITA).count();
            long despesaCount = categories.stream().filter(c -> c.getTipo() == TipoTransacao.DESPESA).count();

            assertEquals(3, receitaCount, "Must have exactly 3 RECEITA categories");
            assertEquals(7, despesaCount, "Must have exactly 7 DESPESA categories");
        }

        @Test
        @DisplayName("REQ-1.2: Registration fails on duplicate email or username (400 Bad Request)")
        public void testDuplicateRegistrationRejected() throws Exception {
            RegisterRequest req1 = new RegisterRequest("user1", "duplicate@example.com", "pass123", "User One");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req1)))
                    .andExpect(status().isCreated());

            // Duplicate Email
            RegisterRequest reqDuplicateEmail = new RegisterRequest("user2", "duplicate@example.com", "pass123", "User Two");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqDuplicateEmail)))
                    .andExpect(status().isBadRequest());

            // Duplicate Username
            RegisterRequest reqDuplicateUser = new RegisterRequest("user1", "other@example.com", "pass123", "User Three");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqDuplicateUser)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // REQUIREMENT 2: Login & JWT Token Generation
    // =========================================================================
    @Nested
    @DisplayName("2. Login & JWT Token Suite")
    class LoginAndJwtTests {

        @Test
        @DisplayName("REQ-2.1: Login with valid credentials returns JWT token and user profile")
        public void testSuccessfulLoginAndMeProfile() throws Exception {
            RegisterRequest regReq = new RegisterRequest("alice", "alice@example.com", "password123", "Alice Smith");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regReq)))
                    .andExpect(status().isCreated());

            LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.user.username").value("alice"))
                    .andReturn();

            String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

            // Verify /api/auth/me
            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("alice@example.com"))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.name").value("Alice Smith"));
        }

        @Test
        @DisplayName("REQ-2.2: Login fails with invalid password or non-existent email (401 Unauthorized)")
        public void testInvalidLoginCredentials() throws Exception {
            RegisterRequest regReq = new RegisterRequest("bob", "bob@example.com", "secret123", "Bob Jones");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regReq)))
                    .andExpect(status().isCreated());

            // Wrong Password
            LoginRequest wrongPass = new LoginRequest("bob@example.com", "wrongpass");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPass)))
                    .andExpect(status().isUnauthorized());

            // Non-existent Email
            LoginRequest wrongEmail = new LoginRequest("nobody@example.com", "secret123");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongEmail)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // REQUIREMENT 3: Multi-Tenant Isolation Verification
    // =========================================================================
    @Nested
    @DisplayName("3. Multi-Tenant Isolation & FK Injection Suite")
    class MultiTenantIsolationTests {

        private Usuario userA;
        private Usuario userB;
        private String tokenA;
        private String tokenB;
        private Conta contaA;
        private Categoria categoriaA;
        private Transacao transacaoA;

        private Conta contaB;
        private Categoria categoriaB;

        @BeforeEach
        public void setupTenants() {
            userA = usuarioRepository.save(new Usuario("tenantA", "tenantA@test.com", passwordEncoder.encode("pass"), "Tenant A"));
            tokenA = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userA));

            userB = usuarioRepository.save(new Usuario("tenantB", "tenantB@test.com", passwordEncoder.encode("pass"), "Tenant B"));
            tokenB = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userB));

            contaA = contaRepository.save(new Conta("Conta Tenant A", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA));
            categoriaA = categoriaRepository.save(new Categoria("Categoria Tenant A", TipoTransacao.DESPESA, userA));
            transacaoA = transacaoRepository.save(new Transacao("Transacao Tenant A", new BigDecimal("50.00"), LocalDate.now(), TipoTransacao.DESPESA, contaA, categoriaA, userA));

            contaB = contaRepository.save(new Conta("Conta Tenant B", new BigDecimal("2000.00"), TipoConta.POUPANCA, userB));
            categoriaB = categoriaRepository.save(new Categoria("Categoria Tenant B", TipoTransacao.RECEITA, userB));
        }

        @Test
        @DisplayName("REQ-3.1: User B attempting to GET, PUT, or DELETE User A's Conta returns 404 Not Found")
        public void testCrossTenantContaAccessReturns404() throws Exception {
            // GET 404
            mockMvc.perform(get("/api/contas/" + contaA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());

            // PUT 404
            ContaRequest updateReq = new ContaRequest("Hack Conta", new BigDecimal("9999.00"), TipoConta.CORRENTE);
            mockMvc.perform(put("/api/contas/" + contaA.getId())
                            .header("Authorization", "Bearer " + tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isNotFound());

            // DELETE 404
            mockMvc.perform(delete("/api/contas/" + contaA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("REQ-3.2: User B attempting to GET, PUT, or DELETE User A's Categoria returns 404 Not Found")
        public void testCrossTenantCategoriaAccessReturns404() throws Exception {
            mockMvc.perform(get("/api/categorias/" + categoriaA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/categorias/" + categoriaA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("REQ-3.3: User B attempting to GET, PUT, or DELETE User A's Transacao returns 404 Not Found")
        public void testCrossTenantTransacaoAccessReturns404() throws Exception {
            mockMvc.perform(get("/api/transacoes/" + transacaoA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/transacoes/" + transacaoA.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("REQ-3.4: Cross-Tenant FK Injection Guard prevents User A from referencing User B's Account or Category")
        public void testCrossTenantFKInjectionGuard() throws Exception {
            // User A creates transaction using User B's Account ID
            TransacaoRequest hackAccountReq = new TransacaoRequest(
                    "FK Injection Account", new BigDecimal("100.00"), LocalDate.now(),
                    TipoTransacao.DESPESA, contaB.getId(), categoriaA.getId()
            );

            mockMvc.perform(post("/api/transacoes")
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(hackAccountReq)))
                    .andExpect(status().isNotFound());

            // Verify User B's account balance unchanged
            Conta refreshedContaB = contaRepository.findById(contaB.getId()).orElseThrow();
            assertEquals(new BigDecimal("2000.00"), refreshedContaB.getSaldoInicial());

            // User A creates transaction using User B's Category ID
            TransacaoRequest hackCategoryReq = new TransacaoRequest(
                    "FK Injection Category", new BigDecimal("100.00"), LocalDate.now(),
                    TipoTransacao.DESPESA, contaA.getId(), categoriaB.getId()
            );

            mockMvc.perform(post("/api/transacoes")
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(hackCategoryReq)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // REQUIREMENT 4: Balance Calculation Verification
    // =========================================================================
    @Nested
    @DisplayName("4. Balance Engine & Accounting Accuracy Suite")
    class BalanceEngineTests {

        private Usuario user;
        private String token;
        private Conta conta1;
        private Conta conta2;
        private Categoria catReceita;
        private Categoria catDespesa;

        @BeforeEach
        public void setupAccount() {
            user = usuarioRepository.save(new Usuario("balance_user", "balance@test.com", passwordEncoder.encode("pass"), "Balance User"));
            token = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(user));

            conta1 = contaRepository.save(new Conta("Conta Principal", new BigDecimal("1000.00"), TipoConta.CORRENTE, user));
            conta2 = contaRepository.save(new Conta("Conta Poupança", new BigDecimal("500.00"), TipoConta.POUPANCA, user));

            catReceita = categoriaRepository.save(new Categoria("Salário", TipoTransacao.RECEITA, user));
            catDespesa = categoriaRepository.save(new Categoria("Mercado", TipoTransacao.DESPESA, user));
        }

        @Test
        @DisplayName("REQ-4.1: Full Balance Lifecycle — Create RECEITA, Create DESPESA, Update, Transfer across accounts, Delete")
        public void testFullBalanceEngineLifecycle() throws Exception {
            // Initial Balance: conta1 = 1000.00

            // 1. Create RECEITA (+500.00) -> expected 1500.00
            TransacaoRequest recReq = new TransacaoRequest("Salário Mensal", new BigDecimal("500.00"), LocalDate.now(), TipoTransacao.RECEITA, conta1.getId(), catReceita.getId());
            MvcResult recResult = mockMvc.perform(post("/api/transacoes")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(recReq)))
                    .andExpect(status().isCreated())
                    .andReturn();
            Long transReceitaId = objectMapper.readTree(recResult.getResponse().getContentAsString()).get("id").asLong();

            Conta c1 = contaRepository.findById(conta1.getId()).orElseThrow();
            assertEquals(new BigDecimal("1500.00"), c1.getSaldoInicial(), "Balance after RECEITA +500.00 must be 1500.00");

            // 2. Create DESPESA (-200.00) -> expected 1300.00
            TransacaoRequest despReq = new TransacaoRequest("Supermercado", new BigDecimal("200.00"), LocalDate.now(), TipoTransacao.DESPESA, conta1.getId(), catDespesa.getId());
            mockMvc.perform(post("/api/transacoes")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(despReq)))
                    .andExpect(status().isCreated());

            c1 = contaRepository.findById(conta1.getId()).orElseThrow();
            assertEquals(new BigDecimal("1300.00"), c1.getSaldoInicial(), "Balance after DESPESA -200.00 must be 1300.00");

            // 3. Update RECEITA from 500.00 to 700.00 -> expected 1500.00 (1300 + 200)
            TransacaoRequest updateRecReq = new TransacaoRequest("Salário Ajustado", new BigDecimal("700.00"), LocalDate.now(), TipoTransacao.RECEITA, conta1.getId(), catReceita.getId());
            mockMvc.perform(put("/api/transacoes/" + transReceitaId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRecReq)))
                    .andExpect(status().isOk());

            c1 = contaRepository.findById(conta1.getId()).orElseThrow();
            assertEquals(new BigDecimal("1500.00"), c1.getSaldoInicial(), "Balance after RECEITA update to 700.00 must be 1500.00");

            // 4. Transfer RECEITA transaction from conta1 (1500.00) to conta2 (500.00) as DESPESA 300.00
            // conta1: Revert RECEITA 700.00 -> 1500.00 - 700.00 = 800.00
            // conta2: Apply DESPESA 300.00 -> 500.00 - 300.00 = 200.00
            TransacaoRequest transferReq = new TransacaoRequest("Transferência Despesa", new BigDecimal("300.00"), LocalDate.now(), TipoTransacao.DESPESA, conta2.getId(), catDespesa.getId());
            mockMvc.perform(put("/api/transacoes/" + transReceitaId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transferReq)))
                    .andExpect(status().isOk());

            c1 = contaRepository.findById(conta1.getId()).orElseThrow();
            Conta c2 = contaRepository.findById(conta2.getId()).orElseThrow();

            assertEquals(new BigDecimal("800.00"), c1.getSaldoInicial(), "Conta 1 balance after transfer out must be 800.00");
            assertEquals(new BigDecimal("200.00"), c2.getSaldoInicial(), "Conta 2 balance after receiving DESPESA 300.00 must be 200.00");

            // 5. Delete transaction on conta2 -> DESPESA 300.00 reverted -> conta2 balance becomes 500.00
            mockMvc.perform(delete("/api/transacoes/" + transReceitaId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            c2 = contaRepository.findById(conta2.getId()).orElseThrow();
            assertEquals(new BigDecimal("500.00"), c2.getSaldoInicial(), "Conta 2 balance after transaction deletion must revert to 500.00");
        }
    }

    // =========================================================================
    // REQUIREMENT 5: Invalid JWT or Missing Authorization Header Rejection
    // =========================================================================
    @Nested
    @DisplayName("5. JWT Validation & Security Rejection Suite")
    class JwtSecurityValidationTests {

        @Test
        @DisplayName("REQ-5.1: Missing Authorization header on protected endpoint is rejected")
        public void testMissingAuthorizationHeaderRejected() throws Exception {
            mockMvc.perform(get("/api/contas"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("REQ-5.2: Malformed JWT token string is rejected")
        public void testMalformedJwtTokenRejected() throws Exception {
            mockMvc.perform(get("/api/contas")
                            .header("Authorization", "Bearer invalid.jwt.string.here"))
                    .andExpect(status().is4xxClientError());

            mockMvc.perform(get("/api/contas")
                            .header("Authorization", "Basic username:password"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("REQ-5.3: Tampered JWT token signature is rejected")
        public void testTamperedJwtSignatureRejected() throws Exception {
            RegisterRequest regReq = new RegisterRequest("tamper_user", "tamper@test.com", "password123", "Tamper User");
            MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regReq)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String validToken = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();
            String tamperedToken = validToken.substring(0, validToken.length() - 4) + "X9Z0";

            assertFalse(tokenProvider.validateToken(tamperedToken), "JwtTokenProvider must report tampered token as invalid");

            mockMvc.perform(get("/api/contas")
                            .header("Authorization", "Bearer " + tamperedToken))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("REQ-5.4: Expired JWT token is rejected")
        public void testExpiredJwtTokenRejected() throws Exception {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date pastDate = new Date(System.currentTimeMillis() - 7200000); // 2 hours ago
            Date pastExpiry = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago

            String expiredToken = Jwts.builder()
                    .subject("tamper@test.com")
                    .claim("userId", 99L)
                    .claim("username", "tamper_user")
                    .issuedAt(pastDate)
                    .expiration(pastExpiry)
                    .signWith(key)
                    .compact();

            assertFalse(tokenProvider.validateToken(expiredToken), "JwtTokenProvider must report expired token as invalid");

            mockMvc.perform(get("/api/contas")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().is4xxClientError());
        }
    }
}
