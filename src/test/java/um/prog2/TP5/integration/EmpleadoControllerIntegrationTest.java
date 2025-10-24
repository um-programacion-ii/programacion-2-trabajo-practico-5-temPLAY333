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
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.repository.DepartamentoRepository;
import um.prog2.TP5.repository.EmpleadoRepository;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para EmpleadoController.
 * Prueba la integración completa del stack web incluyendo validaciones,
 * serialización JSON y manejo de errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("EmpleadoController - Tests de Integración")
class EmpleadoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Departamento departamento;

    @BeforeEach
    void setUp() {
        empleadoRepository.deleteAll();
        departamentoRepository.deleteAll();

        // Crear departamento de prueba
        departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento = departamentoRepository.save(departamento);
    }

    @Test
    @DisplayName("GET /api/empleados - Debería obtener todos los empleados")
    void obtenerTodos_conEmpleadosExistentes_deberiaRetornarListaCompleta() throws Exception {
        // Given
        Empleado empleado1 = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@test.com", new BigDecimal("45000.00"), departamento);
        Empleado empleado2 = TestDataFactory.crearEmpleado("María", "García", "maria@test.com", new BigDecimal("42000.00"), departamento);
        empleadoRepository.save(empleado1);
        empleadoRepository.save(empleado2);

        // When & Then
        mockMvc.perform(get("/api/empleados")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("juan@test.com", "maria@test.com")));
    }

    @Test
    @DisplayName("GET /api/empleados/{id} - Debería obtener empleado por ID existente")
    void obtenerPorId_empleadoExistente_deberiaRetornarEmpleado() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@test.com", new BigDecimal("45000.00"), departamento);
        Empleado guardado = empleadoRepository.save(empleado);

        // When & Then
        mockMvc.perform(get("/api/empleados/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Juan")))
                .andExpect(jsonPath("$.apellido", is("Pérez")))
                .andExpect(jsonPath("$.email", is("juan@test.com")));
    }

    @Test
    @DisplayName("POST /api/empleados - Debería crear nuevo empleado válido")
    void crear_empleadoValido_deberiaCrearYRetornar201() throws Exception {
        // Given
        Empleado nuevoEmpleado = new Empleado();
        nuevoEmpleado.setNombre("Ana");
        nuevoEmpleado.setApellido("López");
        nuevoEmpleado.setEmail("ana@test.com");
        nuevoEmpleado.setSalario(new BigDecimal("45000.00"));
        nuevoEmpleado.setFechaContratacion(LocalDate.now());
        nuevoEmpleado.setDepartamento(departamento);

        String jsonContent = objectMapper.writeValueAsString(nuevoEmpleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Ana")))
                .andExpect(jsonPath("$.apellido", is("López")))
                .andExpect(jsonPath("$.email", is("ana@test.com")));
    }

    @Test
    @DisplayName("POST /api/empleados - Debería retornar 400 para email inválido")
    void crear_empleadoConEmailInvalido_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleadoInvalido = new Empleado();
        empleadoInvalido.setNombre("Pedro");
        empleadoInvalido.setApellido("García");
        empleadoInvalido.setEmail("email-invalido"); // Email sin formato válido
        empleadoInvalido.setSalario(new BigDecimal("45000.00"));
        empleadoInvalido.setFechaContratacion(LocalDate.now());
        empleadoInvalido.setDepartamento(departamento);

        String jsonContent = objectMapper.writeValueAsString(empleadoInvalido);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/empleados/{id} - Debería actualizar empleado existente")
    void actualizar_empleadoExistente_deberiaActualizarYRetornar200() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleado("Carlos", "Ruiz", "carlos@test.com", new BigDecimal("45000.00"), departamento);
        Empleado guardado = empleadoRepository.save(empleado);

        guardado.setNombre("Carlos Alberto");
        guardado.setSalario(new BigDecimal("55000.00"));
        String jsonContent = objectMapper.writeValueAsString(guardado);

        // When & Then
        mockMvc.perform(put("/api/empleados/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre", is("Carlos Alberto")))
                .andExpect(jsonPath("$.salario", is(55000.00)));
    }

    @Test
    @DisplayName("DELETE /api/empleados/{id} - Debería eliminar empleado existente")
    void eliminar_empleadoExistente_deberiaEliminarYRetornar204() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleado("Luis", "Martín", "luis@test.com", new BigDecimal("40000.00"), departamento);
        Empleado guardado = empleadoRepository.save(empleado);

        // When & Then
        mockMvc.perform(delete("/api/empleados/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/empleados/{id}", guardado.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/empleados/email/{email} - Debería obtener empleado por email")
    void obtenerPorEmail_empleadoExistente_deberiaRetornarEmpleado() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleado("Sofia", "Morales", "sofia@test.com", new BigDecimal("43000.00"), departamento);
        empleadoRepository.save(empleado);

        // When & Then
        mockMvc.perform(get("/api/empleados/email/{email}", "sofia@test.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("sofia@test.com")))
                .andExpect(jsonPath("$.nombre", is("Sofia")));
    }

    @Test
    @DisplayName("GET /api/empleados/departamento/{nombre} - Debería obtener empleados por departamento")
    void obtenerPorDepartamento_departamentoConEmpleados_deberiaRetornarLista() throws Exception {
        // Given
        Empleado empleado1 = TestDataFactory.crearEmpleado("Roberto", "Silva", "roberto@test.com", new BigDecimal("44000.00"), departamento);
        Empleado empleado2 = TestDataFactory.crearEmpleado("Carmen", "Torres", "carmen@test.com", new BigDecimal("46000.00"), departamento);
        empleadoRepository.save(empleado1);
        empleadoRepository.save(empleado2);

        // When & Then
        mockMvc.perform(get("/api/empleados/departamento/{nombre}", departamento.getNombre())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].departamento.nombre", everyItem(is(departamento.getNombre()))));
    }

    @Test
    @DisplayName("Test con salario negativo - Debería retornar 400")
    void crear_empleadoConSalarioNegativo_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleadoInvalido = new Empleado();
        empleadoInvalido.setNombre("Test");
        empleadoInvalido.setApellido("User");
        empleadoInvalido.setEmail("test@test.com");
        empleadoInvalido.setSalario(new BigDecimal("-1000.00")); // Salario negativo
        empleadoInvalido.setFechaContratacion(LocalDate.now());
        empleadoInvalido.setDepartamento(departamento);

        String jsonContent = objectMapper.writeValueAsString(empleadoInvalido);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test de empleado inexistente - Debería retornar 404")
    void obtenerPorId_empleadoInexistente_deberiaRetornar404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/empleados/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
