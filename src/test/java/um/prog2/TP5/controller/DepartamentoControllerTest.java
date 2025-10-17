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
import um.prog2.TP5.exception.GlobalExceptionHandler;
import um.prog2.TP5.exception.ResourceNotFoundException;
import um.prog2.TP5.service.DepartamentoService;
import um.prog2.TP5.testutil.TestDataFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DepartamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartamentoService departamentoService;

    @InjectMocks
    private DepartamentoController departamentoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(departamentoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerTodos_deberiaRetornarListaDepartamentos() throws Exception {
        // Given
        List<Departamento> departamentos = Arrays.asList(
                TestDataFactory.crearDepartamentoDesarrollo(),
                TestDataFactory.crearDepartamentoVentas()
        );
        when(departamentoService.obtenerTodos()).thenReturn(departamentos);

        // When & Then
        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Desarrollo"))
                .andExpect(jsonPath("$[1].nombre").value("Ventas"));

        verify(departamentoService).obtenerTodos();
    }

    @Test
    void obtenerPorId_departamentoExistente_deberiaRetornarDepartamento() throws Exception {
        // Given
        Long id = 1L;
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(id);
        when(departamentoService.buscarPorId(id)).thenReturn(departamento);

        // When & Then
        mockMvc.perform(get("/api/departamentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Desarrollo"));

        verify(departamentoService).buscarPorId(id);
    }

    @Test
    void obtenerPorId_departamentoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        when(departamentoService.buscarPorId(id))
                .thenThrow(new ResourceNotFoundException("Departamento no encontrado con ID: " + id));

        // When & Then
        mockMvc.perform(get("/api/departamentos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(departamentoService).buscarPorId(id);
    }

    @Test
    void crear_departamentoValido_deberiaRetornar201() throws Exception {
        // Given
        Departamento departamentoNuevo = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento departamentoGuardado = TestDataFactory.crearDepartamentoDesarrollo();
        departamentoGuardado.setId(1L);

        when(departamentoService.guardar(any(Departamento.class))).thenReturn(departamentoGuardado);

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departamentoNuevo)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Desarrollo"));

        verify(departamentoService).guardar(any(Departamento.class));
    }

    @Test
    void crear_departamentoConNombreVacio_deberiaRetornar400() throws Exception {
        // Given
        Departamento departamento = new Departamento();
        departamento.setNombre(""); // Nombre inválido
        departamento.setDescripcion("Descripción test");

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departamento)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(departamentoService, never()).guardar(any());
    }

    @Test
    void crear_departamentoConNombreCorto_deberiaRetornar400() throws Exception {
        // Given
        Departamento departamento = new Departamento();
        departamento.setNombre("IT"); // Menos de 3 caracteres
        departamento.setDescripcion("Descripción test");

        // When & Then
        mockMvc.perform(post("/api/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departamento)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(departamentoService, never()).guardar(any());
    }

    @Test
    void actualizar_departamentoExistente_deberiaRetornarDepartamentoActualizado() throws Exception {
        // Given
        Long id = 1L;
        Departamento departamentoActualizado = TestDataFactory.crearDepartamentoDesarrollo();
        departamentoActualizado.setId(id);
        departamentoActualizado.setNombre("Desarrollo Actualizado");

        when(departamentoService.actualizar(eq(id), any(Departamento.class)))
                .thenReturn(departamentoActualizado);

        // When & Then
        mockMvc.perform(put("/api/departamentos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departamentoActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Desarrollo Actualizado"));

        verify(departamentoService).actualizar(eq(id), any(Departamento.class));
    }

    @Test
    void eliminar_departamentoExistente_deberiaRetornar204() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(departamentoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/departamentos/{id}", id))
                .andExpect(status().isNoContent());

        verify(departamentoService).eliminar(id);
    }

    @Test
    void eliminar_departamentoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        doThrow(new ResourceNotFoundException("Departamento no encontrado con ID: " + id))
                .when(departamentoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/departamentos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(departamentoService).eliminar(id);
    }

    @Test
    void obtenerPorNombre_departamentoExistente_deberiaRetornarDepartamento() throws Exception {
        // Given
        String nombre = "Desarrollo";
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        departamento.setId(1L);
        when(departamentoService.buscarPorNombre(nombre)).thenReturn(departamento);

        // When & Then
        mockMvc.perform(get("/api/departamentos/nombre/{nombre}", nombre))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value(nombre));

        verify(departamentoService).buscarPorNombre(nombre);
    }

    @Test
    void crear_conContentTypeXml_deberiaRetornar415() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/departamentos")
                .contentType(MediaType.APPLICATION_XML)
                .content("<departamento><nombre>Test</nombre></departamento>"))
                .andExpect(status().isUnsupportedMediaType());

        verify(departamentoService, never()).guardar(any());
    }

    @Test
    void obtenerTodos_conAcceptXml_deberiaRetornar406() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/departamentos")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }
}
