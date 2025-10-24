package um.prog2.TP5.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.exception.GlobalExceptionHandler;
import um.prog2.TP5.exception.ResourceNotFoundException;
import um.prog2.TP5.service.EmpleadoService;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class EmpleadoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmpleadoService empleadoService;

    @InjectMocks
    private EmpleadoController empleadoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(empleadoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerTodos_deberiaRetornarListaDeEmpleados() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado1 = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado1.setId(1L);

        Empleado empleado2 = TestDataFactory.crearEmpleado("María", "López", "maria@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado2.setId(2L);

        List<Empleado> empleados = Arrays.asList(empleado1, empleado2);

        when(empleadoService.obtenerTodos()).thenReturn(empleados);

        // When & Then
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[1].nombre").value("María"));

        verify(empleadoService).obtenerTodos();
    }

    @Test
    void obtenerPorId_empleadoExistente_deberiaRetornarEmpleado() throws Exception {
        // Given
        Long id = 1L;
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado.setId(id);

        when(empleadoService.buscarPorId(id)).thenReturn(empleado);

        // When & Then
        mockMvc.perform(get("/api/empleados/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@empresa.com"));

        verify(empleadoService).buscarPorId(id);
    }

    @Test
    void obtenerPorId_empleadoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        when(empleadoService.buscarPorId(id))
                .thenThrow(new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        // When & Then
        mockMvc.perform(get("/api/empleados/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(empleadoService).buscarPorId(id);
    }

    @Test
    void crear_empleadoValido_deberiaRetornar201() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleadoNuevo = TestDataFactory.crearEmpleado("Nuevo", "Empleado", "nuevo@empresa.com",  BigDecimal.valueOf(100.00), departamento);

        Empleado empleadoGuardado = TestDataFactory.crearEmpleado("Nuevo", "Empleado", "nuevo@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleadoGuardado.setId(1L);

        when(empleadoService.validarEmpleado(any(Empleado.class))).thenReturn(empleadoGuardado);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empleadoNuevo)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.email").value("nuevo@empresa.com"));

        verify(empleadoService).validarEmpleado(any(Empleado.class));
    }

    @Test
    void crear_empleadoConEmailInvalido_deberiaRetornar400() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado = new Empleado();
        empleado.setNombre("Juan");
        empleado.setApellido("Pérez");
        empleado.setEmail("email-invalido"); // Email inválido
        empleado.setFechaContratacion(LocalDate.now());
        empleado.setSalario(new BigDecimal("30000"));
        empleado.setDepartamento(departamento);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empleado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(empleadoService, never()).validarEmpleado(any());
    }

    @Test
    void actualizar_empleadoExistente_deberiaRetornarEmpleadoActualizado() throws Exception {
        // Given
        Long id = 1L;
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleadoActualizado = TestDataFactory.crearEmpleado("Juan", "Pérez Actualizado", "juan@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleadoActualizado.setId(id);

        when(empleadoService.actualizar(eq(id), any(Empleado.class))).thenReturn(empleadoActualizado);

        // When & Then
        mockMvc.perform(put("/api/empleados/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empleadoActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.apellido").value("Pérez Actualizado"));

        verify(empleadoService).actualizar(eq(id), any(Empleado.class));
    }

    @Test
    void eliminar_empleadoExistente_deberiaRetornar204() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(empleadoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/empleados/{id}", id))
                .andExpect(status().isNoContent());

        verify(empleadoService).eliminar(id);
    }

    @Test
    void eliminar_empleadoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        doThrow(new ResourceNotFoundException("Empleado no encontrado con ID: " + id))
                .when(empleadoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/empleados/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(empleadoService).eliminar(id);
    }

    @Test
    void obtenerPorDepartamento_deberiaRetornarListaDeEmpleados() throws Exception {
        // Given
        String nombreDepartamento = "Desarrollo";
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado1 = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado1.setId(1L);

        Empleado empleado2 = TestDataFactory.crearEmpleado("María", "López", "maria@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado2.setId(2L);

        List<Empleado> empleados = Arrays.asList(empleado1, empleado2);

        when(empleadoService.buscarPorDepartamento(nombreDepartamento)).thenReturn(empleados);

        // When & Then
        mockMvc.perform(get("/api/empleados/departamento/{nombre}", nombreDepartamento))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[1].nombre").value("María"));

        verify(empleadoService).buscarPorDepartamento(nombreDepartamento);
    }

    @Test
    void obtenerPorRangoSalario_deberiaRetornarListaDeEmpleados() throws Exception {
        // Given
        BigDecimal min = new BigDecimal("30000");
        BigDecimal max = new BigDecimal("50000");

        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado1 = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado1.setId(1L);
        empleado1.setSalario(new BigDecimal("35000"));

        Empleado empleado2 = TestDataFactory.crearEmpleado("María", "López", "maria@empresa.com",  BigDecimal.valueOf(100.00), departamento);
        empleado2.setId(2L);
        empleado2.setSalario(new BigDecimal("45000"));

        List<Empleado> empleados = Arrays.asList(empleado1, empleado2);

        when(empleadoService.buscarPorRangoSalario(min, max)).thenReturn(empleados);

        // When & Then
        mockMvc.perform(get("/api/empleados/salario")
                .param("salarioMinimo", min.toString())
                .param("salarioMaximo", max.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].salario").value(35000))
                .andExpect(jsonPath("$[1].salario").value(45000));

        verify(empleadoService).buscarPorRangoSalario(min, max);
    }

    @Test
    void crear_conContentTypeXml_deberiaRetornar415() throws Exception {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);

        Empleado empleado = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@empresa.com", BigDecimal.valueOf(100.00), departamento);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_XML)
                .content("<empleado><nombre>Juan</nombre></empleado>"))
                .andExpect(status().isUnsupportedMediaType());

        verify(empleadoService, never()).validarEmpleado(any());
    }
}
