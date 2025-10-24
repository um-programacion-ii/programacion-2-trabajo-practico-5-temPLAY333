package um.prog2.TP5.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.repository.DepartamentoRepository;
import um.prog2.TP5.testutil.TestDataFactory;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para DepartamentoController.
 * Utiliza @SpringBootTest para cargar todo el contexto de la aplicación
 * y probar la integración completa entre controlador, servicio y repositorio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("DepartamentoController - Tests de Integración")
class DepartamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        departamentoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/departamentos - Debería obtener todos los departamentos")
    void obtenerTodos_conDepartamentosExistentes_deberiaRetornarListaCompleta() throws Exception {
        // Given
        Departamento depto1 = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento depto2 = TestDataFactory.crearDepartamentoMarketing();
        departamentoRepository.save(depto1);
        departamentoRepository.save(depto2);

        // When & Then
        mockMvc.perform(get("/api/departamentos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", oneOf("Desarrollo", "Marketing")))
                .andExpect(jsonPath("$[1].nombre", oneOf("Desarrollo", "Marketing")));
    }

    @Test
    @DisplayName("GET /api/departamentos/{id} - Debería obtener departamento por ID existente")
    void obtenerPorId_departamentoExistente_deberiaRetornarDepartamento() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento guardado = departamentoRepository.save(departamento);

        // When & Then
        mockMvc.perform(get("/api/departamentos/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Desarrollo")))
                .andExpect(jsonPath("$.descripcion", is("Descripción del departamento Desarrollo")));
    }

    @Test
    @DisplayName("GET /api/departamentos/{id} - Debería retornar 404 para ID inexistente")
    void obtenerPorId_departamentoInexistente_deberiaRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/departamentos/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/departamentos - Debería crear nuevo departamento válido")
    void crear_departamentoValido_deberiaCrearYRetornar201() throws Exception {
        // Given
        Departamento nuevoDepartamento = TestDataFactory.crearDepartamento("Finanzas");
        String jsonContent = objectMapper.writeValueAsString(nuevoDepartamento);

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Finanzas")))
                .andExpect(jsonPath("$.descripcion", is("Descripción del departamento Finanzas")));
    }

    @Test
    @DisplayName("POST /api/departamentos - Debería retornar 400 para departamento inválido")
    void crear_departamentoInvalido_deberiaRetornar400() throws Exception {
        // Given - Departamento con nombre muy corto (menos de 3 caracteres)
        Departamento departamentoInvalido = new Departamento();
        departamentoInvalido.setNombre("IT"); // Solo 2 caracteres
        departamentoInvalido.setDescripcion("Descripción válida");

        String jsonContent = objectMapper.writeValueAsString(departamentoInvalido);

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/departamentos/{id} - Debería actualizar departamento existente")
    void actualizar_departamentoExistente_deberiaActualizarYRetornar200() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento guardado = departamentoRepository.save(departamento);

        guardado.setNombre("Desarrollo Actualizado");
        guardado.setDescripcion("Nueva descripción");
        String jsonContent = objectMapper.writeValueAsString(guardado);

        // When & Then
        mockMvc.perform(put("/api/departamentos/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Desarrollo Actualizado")))
                .andExpect(jsonPath("$.descripcion", is("Nueva descripción")));
    }

    @Test
    @DisplayName("DELETE /api/departamentos/{id} - Debería eliminar departamento existente")
    void eliminar_departamentoExistente_deberiaEliminarYRetornar204() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento guardado = departamentoRepository.save(departamento);

        // When & Then
        mockMvc.perform(delete("/api/departamentos/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/departamentos/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/departamentos/nombre/{nombre} - Debería obtener departamento por nombre")
    void obtenerPorNombre_departamentoExistente_deberiaRetornarDepartamento() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoMarketing();
        departamentoRepository.save(departamento);

        // When & Then
        mockMvc.perform(get("/api/departamentos/nombre/{nombre}", "Marketing")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre", is("Marketing")))
                .andExpect(jsonPath("$.descripcion", is("Descripción del departamento Marketing")));
    }

    @Test
    @DisplayName("Test de content-type no soportado - Debería retornar 415")
    void crear_conContentTypeNoSoportado_deberiaRetornar415() throws Exception {
        // Given
        String xmlContent = "<departamento><nombre>Test</nombre></departamento>";

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlContent))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Test con JSON malformado - Debería retornar 400")
    void crear_conJsonMalformado_deberiaRetornar400() throws Exception {
        // Given
        String jsonMalformado = "{ nombre: 'Test', descripcion: }"; // JSON inválido

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest());
    }
}
