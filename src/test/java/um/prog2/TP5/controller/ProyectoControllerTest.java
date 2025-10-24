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
import um.prog2.TP5.dto.AsignacionEmpleadosRequest;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.exception.GlobalExceptionHandler;
import um.prog2.TP5.exception.ResourceNotFoundException;
import um.prog2.TP5.service.ProyectoService;
import um.prog2.TP5.testutil.TestDataFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ProyectoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProyectoService proyectoService;

    @InjectMocks
    private ProyectoController proyectoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(proyectoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerTodos_deberiaRetornarListaProyectos() throws Exception {
        // Given
        List<Proyecto> proyectos = Arrays.asList(
                TestDataFactory.crearProyectoActivo("Proyecto 1"),
                TestDataFactory.crearProyectoSinFechaFin("Proyecto 2")
        );
        when(proyectoService.obtenerTodos()).thenReturn(proyectos);

        // When & Then
        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Proyecto 1"))
                .andExpect(jsonPath("$[1].nombre").value("Proyecto 2"));

        verify(proyectoService).obtenerTodos();
    }

    @Test
    void obtenerPorId_proyectoExistente_deberiaRetornarProyecto() throws Exception {
        // Given
        Long id = 1L;
        Proyecto proyecto = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyecto.setId(id);
        when(proyectoService.buscarPorId(id)).thenReturn(proyecto);

        // When & Then
        mockMvc.perform(get("/api/proyectos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Proyecto 1"));

        verify(proyectoService).buscarPorId(id);
    }

    @Test
    void obtenerPorId_proyectoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        when(proyectoService.buscarPorId(id))
                .thenThrow(new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));

        // When & Then
        mockMvc.perform(get("/api/proyectos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(proyectoService).buscarPorId(id);
    }

    @Test
    void crear_proyectoValido_deberiaRetornar201() throws Exception {
        // Given
        Proyecto proyectoNuevo = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        Proyecto proyectoGuardado = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyectoGuardado.setId(1L);

        when(proyectoService.guardar(any(Proyecto.class))).thenReturn(proyectoGuardado);

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyectoNuevo)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Proyecto 1"));

        verify(proyectoService).guardar(any(Proyecto.class));
    }

    @Test
    void crear_proyectoConNombreVacio_deberiaRetornar400() throws Exception {
        // Given
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(""); // Nombre inválido
        proyecto.setDescripcion("Descripción test");

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyecto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(proyectoService, never()).guardar(any());
    }

    @Test
    void crear_proyectoConNombreCorto_deberiaRetornar400() throws Exception {
        // Given
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("AB"); // Menos de 3 caracteres
        proyecto.setDescripcion("Descripción test");

        // When & Then
        mockMvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyecto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(proyectoService, never()).guardar(any());
    }

    @Test
    void actualizar_proyectoExistente_deberiaRetornarProyectoActualizado() throws Exception {
        // Given
        Long id = 1L;
        Proyecto proyectoActualizado = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyectoActualizado.setId(id);
        proyectoActualizado.setNombre("Sistema de Gestión Actualizado");

        when(proyectoService.actualizar(eq(id), any(Proyecto.class)))
                .thenReturn(proyectoActualizado);

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyectoActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Sistema de Gestión Actualizado"));

        verify(proyectoService).actualizar(eq(id), any(Proyecto.class));
    }

    @Test
    void eliminar_proyectoExistente_deberiaRetornar204() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(proyectoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/proyectos/{id}", id))
                .andExpect(status().isNoContent());

        verify(proyectoService).eliminar(id);
    }

    @Test
    void eliminar_proyectoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long id = 999L;
        doThrow(new ResourceNotFoundException("Proyecto no encontrado con ID: " + id))
                .when(proyectoService).eliminar(id);

        // When & Then
        mockMvc.perform(delete("/api/proyectos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(proyectoService).eliminar(id);
    }

    @Test
    void asignarEmpleados_proyectoExistente_deberiaRetornarProyectoActualizado() throws Exception {
        // Given
        Long proyectoId = 1L;
        List<Long> empleadosIds = Arrays.asList(1L, 2L, 3L);
        AsignacionEmpleadosRequest request = new AsignacionEmpleadosRequest(empleadosIds);

        Proyecto proyectoExistente = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyectoExistente.setId(proyectoId);

        Proyecto proyectoActualizado = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyectoActualizado.setId(proyectoId);

        when(proyectoService.buscarPorId(proyectoId)).thenReturn(proyectoExistente);
        when(proyectoService.asignarEmpleados(proyectoExistente, empleadosIds))
                .thenReturn(proyectoActualizado);

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}/empleados", proyectoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(proyectoId));

        verify(proyectoService).buscarPorId(proyectoId);
        verify(proyectoService).asignarEmpleados(proyectoExistente, empleadosIds);
    }

    @Test
    void asignarEmpleados_proyectoNoExistente_deberiaRetornar404() throws Exception {
        // Given
        Long proyectoId = 999L;
        List<Long> empleadosIds = Arrays.asList(1L, 2L);
        AsignacionEmpleadosRequest request = new AsignacionEmpleadosRequest(empleadosIds);

        when(proyectoService.buscarPorId(proyectoId))
                .thenThrow(new ResourceNotFoundException("Proyecto no encontrado con ID: " + proyectoId));

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}/empleados", proyectoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));

        verify(proyectoService).buscarPorId(proyectoId);
        verify(proyectoService, never()).asignarEmpleados(any(), any());
    }

    @Test
    void asignarEmpleados_listaVacia_deberiaRetornar400() throws Exception {
        // Given
        Long proyectoId = 1L;
        AsignacionEmpleadosRequest request = new AsignacionEmpleadosRequest(Arrays.asList());

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}/empleados", proyectoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(proyectoService, never()).buscarPorId(any());
        verify(proyectoService, never()).asignarEmpleados(any(), any());
    }

    @Test
    void asignarEmpleados_requestSinBody_deberiaRetornar400() throws Exception {
        // Given
        Long proyectoId = 1L;

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}/empleados", proyectoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(proyectoService, never()).buscarPorId(any());
        verify(proyectoService, never()).asignarEmpleados(any(), any());
    }

    @Test
    void crear_conContentTypeTextoPlano_deberiaRetornar415() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/proyectos")
                .contentType(MediaType.TEXT_PLAIN)
                .content("proyecto inválido"))
                .andExpect(status().isUnsupportedMediaType());

        verify(proyectoService, never()).guardar(any());
    }

    @Test
    void obtenerTodos_conAcceptXml_deberiaRetornar406() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/proyectos")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void actualizar_proyectoConFechasInvalidas_deberiaRetornar400() throws Exception {
        // Given
        Long id = 1L;
        Proyecto proyecto = TestDataFactory.crearProyectoSinFechaFin("Proyecto 1");
        proyecto.setFechaInicio(java.time.LocalDate.of(2024, 12, 31));
        proyecto.setFechaFin(java.time.LocalDate.of(2024, 1, 1)); // Fecha fin antes que inicio

        // When & Then
        mockMvc.perform(put("/api/proyectos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proyecto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERROR_VALIDACION"));

        verify(proyectoService, never()).actualizar(any(), any());
    }

    @Test
    void crear_proyectoConJsonMalformado_deberiaRetornar400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\": \"Test\", \"descripcion\":"))
                .andExpect(status().isBadRequest());

        verify(proyectoService, never()).guardar(any());
    }
}
