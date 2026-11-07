package com.desarrolloweb.tp1.controller;

import com.desarrolloweb.tp1.model.Rol;
import com.desarrolloweb.tp1.model.Usuario;
import com.desarrolloweb.tp1.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de punta a punta: PostgreSQL real en un contenedor,
 * migraciones Flyway contra una base limpia, filtro JWT y reglas de
 * autorización de los servicios.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ListaControllerIT {

    private static final String PASSWORD = "password123";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void login_credencialesValidas_devuelve200ConToken() throws Exception {
        registrar("valido@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("valido@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("valido@example.com"))
                .andExpect(jsonPath("$.usuario.rol").value("USER"));
    }

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        registrar("invalido@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("invalido@example.com", "otraPassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaPropia_devuelve200() throws Exception {
        String token = registrarYLogin("propia@example.com");
        Long listaId = crearLista(token, "Mis regalos");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Mis regalos"));
    }

    @Test
    void obtener_listaAjena_devuelve403() throws Exception {
        String tokenDuenio = registrarYLogin("duenio@example.com");
        Long listaId = crearLista(tokenDuenio, "Lista privada");
        String tokenIntruso = registrarYLogin("intruso@example.com");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaAjenaComoAdmin_devuelve200() throws Exception {
        String tokenDuenio = registrarYLogin("duenio2@example.com");
        Long listaId = crearLista(tokenDuenio, "Lista visible para admin");
        String tokenAdmin = promoverAdminYLogin("admin@example.com");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId));
    }

    private void registrar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String registrarYLogin(String email) throws Exception {
        registrar(email);
        return login(email);
    }

    private String promoverAdminYLogin(String email) throws Exception {
        registrar(email);
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        usuarioRepository.save(new Usuario(usuario.id(), usuario.email(), usuario.passwordHash(), Rol.ADMIN));
        return login(email);
    }

    private String login(String email) throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private Long crearLista(String token, String nombre) throws Exception {
        var resultado = mockMvc.perform(post("/api/listas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"%s\"}".formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private String credenciales(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }
}
