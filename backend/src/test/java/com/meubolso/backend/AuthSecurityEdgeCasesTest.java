package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.LoginRequest;
import com.meubolso.backend.dto.RegisterRequest;
import com.meubolso.backend.repository.CategoriaRepository;
import com.meubolso.backend.repository.ContaRepository;
import com.meubolso.backend.repository.TransacaoRepository;
import com.meubolso.backend.repository.UsuarioRepository;
import com.meubolso.backend.security.JwtTokenProvider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthSecurityEdgeCasesTest {

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
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Edge Case 1: Tampered JWT token signature should be rejected")
    public void testTamperedTokenSignature() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("edgeuser", "edge@example.com", "password123", "Edge User");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        assertNotNull(token);

        // Tamper with the token by modifying the signature character
        String tamperedToken = token.substring(0, token.length() - 4) + "X1Y2";

        // Unit validation check
        assertFalse(jwtTokenProvider.validateToken(tamperedToken), "Tampered token should fail validation");

        // MockMvc Endpoint check
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Edge Case 2: Expired JWT token should be rejected")
    public void testExpiredToken() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        Date pastExpiry = new Date(System.currentTimeMillis() - 1800000); // 30 minutes ago

        String expiredToken = Jwts.builder()
                .subject("edge@example.com")
                .claim("userId", 1L)
                .claim("username", "edgeuser")
                .claim("name", "Edge User")
                .issuedAt(pastDate)
                .expiration(pastExpiry)
                .signWith(key)
                .compact();

        // Unit validation check
        assertFalse(jwtTokenProvider.validateToken(expiredToken), "Expired token should fail validation");

        // MockMvc Endpoint check
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Edge Case 3: Invalid credentials (wrong password & non-existent user)")
    public void testInvalidCredentials() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("edgeuser", "edge@example.com", "password123", "Edge User");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Wrong password
        LoginRequest wrongPass = new LoginRequest("edge@example.com", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPass)))
                .andExpect(status().isUnauthorized());

        // Non-existent email
        LoginRequest nonExistent = new LoginRequest("nonexistent@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistent)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case 4: Duplicate email registration should fail")
    public void testDuplicateEmailRegistration() throws Exception {
        RegisterRequest req1 = new RegisterRequest("user1", "duplicate@example.com", "password123", "User One");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        RegisterRequest req2 = new RegisterRequest("user2", "duplicate@example.com", "password123", "User Two");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case 5: Malformed Authorization header / random token string")
    public void testMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer not.a.valid.jwt.token"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "InvalidScheme token"))
                .andExpect(status().is4xxClientError());
    }
}
