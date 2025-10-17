package um.prog2.TP5.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.exception.GlobalExceptionHandler;
import um.prog2.TP5.service.EmpleadoService;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para validación de entrada en EmpleadoController
 */
@ExtendWith(MockitoExtension.class)
class EmpleadoControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private EmpleadoService empleadoService;

    @InjectMocks
    private EmpleadoController empleadoController;

    private ObjectMapper objectMapper;
    private Departamento departamentoValido;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para manejar LocalDate

        mockMvc = MockMvcBuilders
                .standaloneSetup(empleadoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        departamentoValido = TestDataFactory.crearDepartamentoDesarrollo();
        departamentoValido.setId(1L);
    }

    @Test
    void crearEmpleadoValido_deberiaRetornar201() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isCreated());
    }

    @Test
    void crearEmpleadoConNombreVacio_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setNombre(""); // Nombre inválido
        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("nombre")));

        // Verify que el servicio no fue llamado
        verify(empleadoService, never()).validarEmpleado(any());
    }

    @Test
    void crearEmpleadoConEmailInvalido_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setEmail("email-invalido"); // Email sin formato válido
        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("email")));

        verify(empleadoService, never()).validarEmpleado(any());
    }

    @Test
    void crearEmpleadoConSalarioNegativo_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setSalario(new BigDecimal("-1000")); // Salario negativo
        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("salario")));

        verify(empleadoService, never()).validarEmpleado(any());
    }

    @Test
    void crearEmpleadoConFechaContratacionFutura_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setFechaContratacion(LocalDate.now().plusDays(10)); // Fecha futura
        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("fecha")));

        verify(empleadoService, never()).validarEmpleado(any());
    }

    @Test
    void actualizarEmpleadoConDatosInvalidos_deberiaRetornar400() throws Exception {
        // Given
        Empleado empleado = new Empleado();
        empleado.setNombre(""); // Inválido
        empleado.setApellido("A"); // Muy corto
        empleado.setEmail("email-sin-formato"); // Formato inválido
        empleado.setSalario(BigDecimal.ZERO); // Cero no permitido
        empleado.setFechaContratacion(LocalDate.now().plusYears(1)); // Futura

        String empleadoJson = objectMapper.writeValueAsString(empleado);

        // When & Then
        mockMvc.perform(put("/api/empleados/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(empleadoService, never()).actualizar(any(), any());
    }
}
