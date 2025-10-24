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
import um.prog2.TP5.dto.AsignacionEmpleadosRequest;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.repository.DepartamentoRepository;
import um.prog2.TP5.repository.EmpleadoRepository;
import um.prog2.TP5.repository.ProyectoRepository;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProyectoController.
 * Incluye pruebas para la asignación de empleados a proyectos
 * y filtros por estado (activo/inactivo).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProyectoController - Tests de Integración")
class ProyectoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Departamento departamento;
    private Empleado empleado1;
    private Empleado empleado2;

    @BeforeEach
    void setUp() {
        proyectoRepository.deleteAll();
        empleadoRepository.deleteAll();
        departamentoRepository.deleteAll();

        // Crear datos de prueba
        departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento = departamentoRepository.save(departamento);

        empleado1 = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@test.com", new BigDecimal("45000.00"), departamento);
        empleado2 = TestDataFactory.crearEmpleado("María", "García", "maria@test.com", new BigDecimal("42000.00"), departamento);
        empleado1 = empleadoRepository.save(empleado1);
        empleado2 = empleadoRepository.save(empleado2);
    }

    @Test
    @DisplayName("GET /api/proyectos - Debería obtener todos los proyectos")
    void obtenerTodos_conProyectosExistentes_deberiaRetornarListaCompleta() throws Exception {
        // Given
        Proyecto proyecto1 = TestDataFactory.crearProyecto("Sistema Web", LocalDate.now(), LocalDate.now().plusMonths(6));
        Proyecto proyecto2 = TestDataFactory.crearProyecto("App Mobile", LocalDate.now(), LocalDate.now().plusMonths(4));
        proyectoRepository.save(proyecto1);
        proyectoRepository.save(proyecto2);

        // When & Then
        mockMvc.perform(get("/api/proyectos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nombre", containsInAnyOrder("Sistema Web", "App Mobile")));
    }

    @Test
    @DisplayName("GET /api/proyectos/{id} - Debería obtener proyecto por ID existente")
    void obtenerPorId_proyectoExistente_deberiaRetornarProyecto() throws Exception {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyecto("E-commerce", LocalDate.now(), LocalDate.now().plusMonths(8));
        Proyecto guardado = proyectoRepository.save(proyecto);

        // When & Then
        mockMvc.perform(get("/api/proyectos/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("E-commerce")))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    @DisplayName("POST /api/proyectos - Debería crear nuevo proyecto válido")
    void crear_proyectoValido_deberiaCrearYRetornar201() throws Exception {
        // Given
        Proyecto nuevoProyecto = TestDataFactory.crearProyecto("CRM System", LocalDate.now(), LocalDate.now().plusYears(1));
        String jsonContent = objectMapper.writeValueAsString(nuevoProyecto);

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("CRM System")))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    @DisplayName("POST /api/proyectos - Debería retornar 400 para proyecto inválido")
    void crear_proyectoInvalido_deberiaRetornar400() throws Exception {
        // Given - Proyecto con fechas inválidas (fecha fin antes que fecha inicio)
        Proyecto proyectoInvalido = new Proyecto();
        proyectoInvalido.setNombre("Proyecto Inválido");
        proyectoInvalido.setDescripcion("Descripción del proyecto");
        proyectoInvalido.setFechaInicio(LocalDate.now());
        proyectoInvalido.setFechaFin(LocalDate.now().minusDays(1)); // Fecha fin antes que inicio

        String jsonContent = objectMapper.writeValueAsString(proyectoInvalido);

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/proyectos/{id} - Debería actualizar proyecto existente")
    void actualizar_proyectoExistente_deberiaActualizarYRetornar200() throws Exception {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyecto("Portal Web", LocalDate.now(), LocalDate.now().plusMonths(6));
        Proyecto guardado = proyectoRepository.save(proyecto);

        guardado.setNombre("Portal Web Actualizado");
        guardado.setDescripcion("Nueva descripción del portal");
        String jsonContent = objectMapper.writeValueAsString(guardado);

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre", is("Portal Web Actualizado")));
    }

    @Test
    @DisplayName("DELETE /api/proyectos/{id} - Debería eliminar proyecto existente")
    void eliminar_proyectoExistente_deberiaEliminarYRetornar204() throws Exception {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyecto("Proyecto Temporal", LocalDate.now(), LocalDate.now().plusMonths(3));
        Proyecto guardado = proyectoRepository.save(proyecto);

        // When & Then
        mockMvc.perform(delete("/api/proyectos/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/proyectos/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/proyectos/activos - Debería obtener solo proyectos activos")
    void obtenerActivos_conProyectosMixtos_deberiaRetornarSoloActivos() throws Exception {
        // Given
        // Proyecto activo: fecha fin en el futuro
        Proyecto proyectoActivo = TestDataFactory.crearProyecto("Activo", LocalDate.now(), LocalDate.now().plusMonths(6));

        // Proyecto inactivo: fecha fin en el pasado
        Proyecto proyectoInactivo = TestDataFactory.crearProyecto("Inactivo", LocalDate.now().minusMonths(2), LocalDate.now().minusDays(1));

        proyectoRepository.save(proyectoActivo);
        proyectoRepository.save(proyectoInactivo);

        // When & Then
        mockMvc.perform(get("/api/proyectos/activos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Activo")));
    }

    @Test
    @DisplayName("POST /api/proyectos/{id}/asignar-empleados - Debería asignar empleados al proyecto")
    void asignarEmpleados_proyectoYEmpleadosExistentes_deberiaAsignarCorrectamente() throws Exception {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyecto("Proyecto Colaborativo", LocalDate.now(), LocalDate.now().plusMonths(8));
        Proyecto guardado = proyectoRepository.save(proyecto);

        AsignacionEmpleadosRequest request = new AsignacionEmpleadosRequest();
        request.setEmpleadosIds(Arrays.asList(empleado1.getId(), empleado2.getId()));

        String jsonContent = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/proyectos/{id}/asignar-empleados", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.empleados", hasSize(2)))
                .andExpect(jsonPath("$.empleados[*].id", containsInAnyOrder(
                    empleado1.getId().intValue(),
                    empleado2.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/proyectos/{id}/empleados - Debería obtener empleados del proyecto")
    void obtenerEmpleadosDelProyecto_proyectoConEmpleados_deberiaRetornarLista() throws Exception {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyecto("Proyecto Team", LocalDate.now(), LocalDate.now().plusMonths(5));
        proyecto.getEmpleados().add(empleado1);
        proyecto.getEmpleados().add(empleado2);
        Proyecto guardado = proyectoRepository.save(proyecto);

        // When & Then
        mockMvc.perform(get("/api/proyectos/{id}/empleados", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                    empleado1.getId().intValue(),
                    empleado2.getId().intValue())));
    }

    @Test
    @DisplayName("Test de proyecto inexistente - Debería retornar 404")
    void obtenerPorId_proyectoInexistente_deberiaRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/proyectos/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test de asignación a proyecto inexistente - Debería retornar 404")
    void asignarEmpleados_proyectoInexistente_deberiaRetornar404() throws Exception {
        // Given
        AsignacionEmpleadosRequest request = new AsignacionEmpleadosRequest();
        request.setEmpleadosIds(Arrays.asList(empleado1.getId()));
        String jsonContent = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/proyectos/{id}/asignar-empleados", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test con contenido XML - Debería retornar 415")
    void crear_conContentTypeXml_deberiaRetornar415() throws Exception {
        // Given
        String xmlContent = "<proyecto><nombre>Test</nombre></proyecto>";

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlContent))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Test con JSON malformado - Debería retornar 400")
    void crear_conJsonMalformado_deberiaRetornar400() throws Exception {
        // Given
        String jsonMalformado = "{ nombre: 'Test', fechaInicio: }"; // JSON inválido

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest());
    }
}
