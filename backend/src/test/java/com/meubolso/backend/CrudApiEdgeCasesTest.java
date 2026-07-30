package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.CategoriaRequest;
import com.meubolso.backend.dto.ContaRequest;
import com.meubolso.backend.dto.RegisterRequest;
import com.meubolso.backend.dto.TransacaoRequest;
import com.meubolso.backend.entity.Categoria;
import com.meubolso.backend.entity.Conta;
import com.meubolso.backend.entity.TipoConta;
import com.meubolso.backend.entity.TipoTransacao;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class CrudApiEdgeCasesTest {

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
    private String tokenA;
    private Conta contaUserA;
    private Categoria categoriaUserA;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        userA = new Usuario("usera", "usera@test.com", passwordEncoder.encode("password123"), "User A");
        userA = usuarioRepository.save(userA);
        tokenA = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(userA));

        contaUserA = new Conta("Conta Principal", new BigDecimal("1000.00"), TipoConta.CORRENTE, userA);
        contaUserA = contaRepository.save(contaUserA);

        categoriaUserA = new Categoria("Alimentação", TipoTransacao.DESPESA, userA);
        categoriaUserA = categoriaRepository.save(categoriaUserA);
    }

    // ==========================================
    // CONTA EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Conta Validation: Blank name should return 400 Bad Request")
    public void testContaBlankNameValidation() throws Exception {
        ContaRequest request = new ContaRequest("   ", new BigDecimal("500.00"), TipoConta.CORRENTE);

        mockMvc.perform(post("/api/contas")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists());
    }

    @Test
    @DisplayName("Conta Lookup: Non-existent ID lookup should return 404 Not Found")
    public void testContaNonExistentLookup404() throws Exception {
        Long nonExistentId = 99999L;

        // GET 404
        mockMvc.perform(get("/api/contas/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // PUT 404
        ContaRequest updateReq = new ContaRequest("Conta Inexistente", new BigDecimal("100.00"), TipoConta.CORRENTE);
        mockMvc.perform(put("/api/contas/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // DELETE 404
        mockMvc.perform(delete("/api/contas/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // CATEGORIA EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Categoria Validation: Blank name should return 400 Bad Request")
    public void testCategoriaBlankNameValidation() throws Exception {
        CategoriaRequest request = new CategoriaRequest("", TipoTransacao.DESPESA);

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists());
    }

    @Test
    @DisplayName("Categoria Lookup: Non-existent ID lookup should return 404 Not Found")
    public void testCategoriaNonExistentLookup404() throws Exception {
        Long nonExistentId = 99999L;

        // GET 404
        mockMvc.perform(get("/api/categorias/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // PUT 404
        CategoriaRequest updateReq = new CategoriaRequest("Categoria Inexistente", TipoTransacao.DESPESA);
        mockMvc.perform(put("/api/categorias/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // DELETE 404
        mockMvc.perform(delete("/api/categorias/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TRANSACAO EDGE CASES
    // ==========================================

    @Test
    @DisplayName("Transacao Validation: Blank description should return 400 Bad Request")
    public void testTransacaoBlankDescriptionValidation() throws Exception {
        TransacaoRequest request = new TransacaoRequest(
                "",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.descricao").exists());
    }

    @Test
    @DisplayName("Transacao Validation: Zero amount should return 400 Bad Request")
    public void testTransacaoZeroAmountValidation() throws Exception {
        TransacaoRequest request = new TransacaoRequest(
                "Transacao Valor Zero",
                BigDecimal.ZERO,
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.valor").exists());
    }

    @Test
    @DisplayName("Transacao Validation: Negative amount should return 400 Bad Request")
    public void testTransacaoNegativeAmountValidation() throws Exception {
        TransacaoRequest request = new TransacaoRequest(
                "Transacao Valor Negativo",
                new BigDecimal("-25.50"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );

        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.valor").exists());
    }

    @Test
    @DisplayName("Transacao Lookup: Non-existent ID lookup should return 404 Not Found")
    public void testTransacaoNonExistentLookup404() throws Exception {
        Long nonExistentId = 99999L;

        // GET 404
        mockMvc.perform(get("/api/transacoes/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // PUT 404
        TransacaoRequest updateReq = new TransacaoRequest(
                "Atualizar Inexistente",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                categoriaUserA.getId()
        );
        mockMvc.perform(put("/api/transacoes/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // DELETE 404
        mockMvc.perform(delete("/api/transacoes/" + nonExistentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Transacao FK Guard: Non-existent contaId or categoriaId should return 404 Not Found")
    public void testTransacaoNonExistentFKGuard404() throws Exception {
        // Non-existent contaId
        TransacaoRequest invalidContaReq = new TransacaoRequest(
                "Conta Inexistente",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                99999L,
                categoriaUserA.getId()
        );
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidContaReq)))
                .andExpect(status().isNotFound());

        // Non-existent categoriaId
        TransacaoRequest invalidCatReq = new TransacaoRequest(
                "Categoria Inexistente",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransacao.DESPESA,
                contaUserA.getId(),
                99999L
        );
        mockMvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCatReq)))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // DEFAULT CATEGORY SEEDING ON USER REGISTRATION
    // ==========================================

    @Test
    @DisplayName("Default Category Seeding: Registering a user seeds 10 default categories")
    public void testDefaultCategorySeedingOnUserRegistration() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("newuser", "newuser@example.com", "password123", "New User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Retrieve user categories via REST endpoint
        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));

        // Direct DB verification
        Usuario registeredUser = usuarioRepository.findByEmail("newuser@example.com").orElseThrow();
        List<Categoria> userCategories = categoriaRepository.findByUsuarioId(registeredUser.getId());
        assertEquals(10, userCategories.size());

        long receitaCount = userCategories.stream().filter(c -> c.getTipo() == TipoTransacao.RECEITA).count();
        long despesaCount = userCategories.stream().filter(c -> c.getTipo() == TipoTransacao.DESPESA).count();

        assertEquals(3, receitaCount, "Default categories must contain 3 RECEITA categories");
        assertEquals(7, despesaCount, "Default categories must contain 7 DESPESA categories");

        List<String> names = userCategories.stream().map(Categoria::getNome).toList();
        assertTrue(names.containsAll(List.of(
                "Salário", "Investimentos", "Outras Receitas",
                "Alimentação", "Moradia", "Transporte", "Lazer", "Saúde", "Educação", "Outras Despesas"
        )));
    }
}
