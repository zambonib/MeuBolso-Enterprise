package com.meubolso.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meubolso.backend.dto.CategoriaRequest;
import com.meubolso.backend.entity.Categoria;
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
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ContaRepository contaRepository;

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
    @DisplayName("Should create category successfully for authenticated user")
    public void testCreateCategoriaSuccess() throws Exception {
        CategoriaRequest request = new CategoriaRequest("Mercado", TipoTransacao.DESPESA);

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome").value("Mercado"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating category with empty name")
    public void testCreateCategoriaValidationError() throws Exception {
        CategoriaRequest request = new CategoriaRequest("", TipoTransacao.DESPESA);

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists());
    }

    @Test
    @DisplayName("Should return only categories belonging to the authenticated user")
    public void testGetAllCategoriasScopedToUser() throws Exception {
        Categoria catA = new Categoria("Categoria A", TipoTransacao.RECEITA, userA);
        categoriaRepository.save(catA);

        Categoria catB = new Categoria("Categoria B", TipoTransacao.DESPESA, userB);
        categoriaRepository.save(catB);

        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Categoria A"));
    }

    @Test
    @DisplayName("Should get category by ID when owned by authenticated user")
    public void testGetCategoriaByIdSuccess() throws Exception {
        Categoria catA = new Categoria("Categoria A", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        mockMvc.perform(get("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catA.getId()))
                .andExpect(jsonPath("$.nome").value("Categoria A"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when requesting cross-tenant category")
    public void testGetCategoriaByIdCrossTenant404() throws Exception {
        Categoria catA = new Categoria("Categoria A", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        mockMvc.perform(get("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update category successfully when owned by authenticated user")
    public void testUpdateCategoriaSuccess() throws Exception {
        Categoria catA = new Categoria("Cat Antiga", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        CategoriaRequest updateReq = new CategoriaRequest("Cat Nova", TipoTransacao.DESPESA);

        mockMvc.perform(put("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cat Nova"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating cross-tenant category")
    public void testUpdateCategoriaCrossTenant404() throws Exception {
        Categoria catA = new Categoria("Cat A", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        CategoriaRequest updateReq = new CategoriaRequest("Cat Hack", TipoTransacao.DESPESA);

        mockMvc.perform(put("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete category when owned by authenticated user")
    public void testDeleteCategoriaSuccess() throws Exception {
        Categoria catA = new Categoria("Cat A", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        mockMvc.perform(delete("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting cross-tenant category")
    public void testDeleteCategoriaCrossTenant404() throws Exception {
        Categoria catA = new Categoria("Cat A", TipoTransacao.RECEITA, userA);
        catA = categoriaRepository.save(catA);

        mockMvc.perform(delete("/api/categorias/" + catA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
