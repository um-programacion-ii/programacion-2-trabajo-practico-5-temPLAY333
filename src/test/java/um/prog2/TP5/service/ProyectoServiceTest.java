package um.prog2.TP5.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.exception.ProyectoNoEncontradoException;
import um.prog2.TP5.test.base.BaseIntegrationTest;
import um.prog2.TP5.testutil.TestDataFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para ProyectoService
 * Extiende BaseIntegrationTest para obtener limpieza automática y utilidades
 */
class ProyectoServiceTest extends BaseIntegrationTest {

    @Autowired
    private ProyectoService proyectoService;

    @BeforeEach
    void configurarDatosBase() {
        // Crear datos base para todos los tests
        // Esto evita dependencias entre tests y hace cada uno más independiente
        crearProyectosBase();
    }

    @Test
    void guardar_proyectoValido_deberiaGuardarCorrectamente() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Nuevo Proyecto");

        // When
        Proyecto guardado = proyectoService.guardar(proyecto);

        // Then
        assertThat(guardado)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getId()).isNotNull();
                    assertThat(p.getNombre()).isEqualTo("Nuevo Proyecto");
                    assertThat(p.getDescripcion()).isEqualTo("Descripción del proyecto Nuevo Proyecto");
                });
    }

    @Test
    void buscarPorId_proyectoExistente_deberiaRetornarProyecto() {
        // Given - Ya tenemos PROYECTO_ACTIVO_ID creado en @BeforeEach

        // When
        Proyecto encontrado = proyectoService.buscarPorId(PROYECTO_ACTIVO_ID);

        // Then
        assertThat(encontrado)
                .isNotNull()
                .extracting(Proyecto::getNombre)
                .isEqualTo("Proyecto Base Activo");
    }

    @Test
    void buscarPorId_proyectoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> proyectoService.buscarPorId(999L))
                .isInstanceOf(ProyectoNoEncontradoException.class)
                .hasMessageContaining("Proyecto no encontrado con ID: 999");
    }

    @Test
    void obtenerProyectosActivos_deberiaRetornarSoloActivos() {
        // Given - Los datos base ya contienen proyectos activos y finalizados

        // When
        List<Proyecto> proyectosActivos = proyectoService.obtenerProyectosActivos();

        // Then
        assertThat(proyectosActivos)
                .hasSize(2) // Proyecto Base Activo + Proyecto Base Sin Fin
                .extracting(Proyecto::getNombre)
                .containsExactlyInAnyOrder("Proyecto Base Activo", "Proyecto Base Sin Fin");
    }

    @Test
    void obtenerTodos_conVariosProyectos_deberiaRetornarTodos() {
        // Given - Los datos base ya contienen 3 proyectos

        // When
        List<Proyecto> todos = proyectoService.obtenerTodos();

        // Then
        assertThat(todos)
                .hasSize(3) // Los 3 proyectos base
                .extracting(Proyecto::getNombre)
                .containsExactlyInAnyOrder(
                    "Proyecto Base Activo",
                    "Proyecto Base Finalizado",
                    "Proyecto Base Sin Fin"
                );
    }

    @Test
    void actualizar_proyectoExistente_deberiaActualizarCorrectamente() {
        // Given - Usar proyecto base existente
        Proyecto actualizado = TestDataFactory.crearProyectoActivo("Proyecto Actualizado");
        actualizado.setDescripcion("Nueva descripción");

        // When
        Proyecto resultado = proyectoService.actualizar(PROYECTO_ACTIVO_ID, actualizado);

        // Then
        assertThat(resultado)
                .satisfies(p -> {
                    assertThat(p.getId()).isEqualTo(PROYECTO_ACTIVO_ID);
                    assertThat(p.getNombre()).isEqualTo("Proyecto Actualizado");
                    assertThat(p.getDescripcion()).isEqualTo("Nueva descripción");
                });
    }

    @Test
    void actualizar_proyectoInexistente_deberieLanzarExcepcion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto");

        // When & Then
        assertThatThrownBy(() -> proyectoService.actualizar(999L, proyecto))
                .isInstanceOf(ProyectoNoEncontradoException.class)
                .hasMessageContaining("Proyecto no encontrado con ID: 999");
    }

    @Test
    void eliminar_proyectoExistente_deberiaEliminarCorrectamente() {
        // Given - Usar proyecto base existente
        Long idAEliminar = PROYECTO_FINALIZADO_ID;

        // When
        proyectoService.eliminar(idAEliminar);

        // Then
        assertThatThrownBy(() -> proyectoService.buscarPorId(idAEliminar))
                .isInstanceOf(ProyectoNoEncontradoException.class);
    }

    @Test
    void eliminar_proyectoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> proyectoService.eliminar(999L))
                .isInstanceOf(ProyectoNoEncontradoException.class)
                .hasMessageContaining("Proyecto no encontrado con ID: 999");
    }
}
