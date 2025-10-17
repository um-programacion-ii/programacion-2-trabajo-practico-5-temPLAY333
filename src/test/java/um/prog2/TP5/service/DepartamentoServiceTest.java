package um.prog2.TP5.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.exception.DepartamentoDuplicadoException;
import um.prog2.TP5.exception.DepartamentoNoEncontradoException;
import um.prog2.TP5.test.base.BaseIntegrationTest;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para DepartamentoService
 * Extiende BaseIntegrationTest para obtener limpieza automática y utilidades
 */
class DepartamentoServiceTest extends BaseIntegrationTest {

    @Autowired
    private DepartamentoService departamentoService;

    @BeforeEach
    void configurarDatosBase() {
        // Crear datos base para todos los tests
        // Esto evita dependencias entre tests y hace cada uno más independiente
        crearDepartamentosBase();
    }

    @Test
    void guardar_departamentoValido_deberiaGuardarCorrectamente() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamento("Finanzas");

        // When
        Departamento guardado = departamentoService.guardar(departamento);

        // Then
        assertThat(guardado)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isNotNull();
                    assertThat(d.getNombre()).isEqualTo("Finanzas");
                    assertThat(d.getDescripcion()).isEqualTo("Descripción del departamento Finanzas");
                });
    }

    @Test
    void guardar_departamentoConNombreDuplicado_deberieLanzarExcepcion() {
        // Given - Intentar crear departamento con nombre que ya existe
        Departamento departamentoDuplicado = TestDataFactory.crearDepartamento("Desarrollo");

        // When & Then
        assertThatThrownBy(() -> departamentoService.guardar(departamentoDuplicado))
                .isInstanceOf(DepartamentoDuplicadoException.class)
                .hasMessageContaining("Ya existe un departamento con el nombre: Desarrollo");
    }

    @Test
    void buscarPorId_departamentoExistente_deberiaRetornarDepartamento() {
        // Given - Usar datos base en lugar de crear nuevos
        // Ya tenemos DEPARTAMENTO_DESARROLLO_ID creado en @BeforeEach

        // When
        Departamento encontrado = departamentoService.buscarPorId(DEPARTAMENTO_DESARROLLO_ID);

        // Then
        assertThat(encontrado)
                .isNotNull()
                .extracting(Departamento::getNombre)
                .isEqualTo("Desarrollo");
    }

    @Test
    void buscarPorId_departamentoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> departamentoService.buscarPorId(999L))
                .isInstanceOf(DepartamentoNoEncontradoException.class)
                .hasMessageContaining("Departamento no encontrado con ID: 999");
    }

    @Test
    void buscarPorNombre_departamentoExistente_deberiaRetornarDepartamento() {
        // Given - Usar datos base

        // When
        Departamento encontrado = departamentoService.buscarPorNombre("Marketing");

        // Then
        assertThat(encontrado)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(DEPARTAMENTO_MARKETING_ID);
                    assertThat(d.getNombre()).isEqualTo("Marketing");
                    assertThat(d.getDescripcion()).isEqualTo("Descripción del departamento Marketing");
                });
    }

    @Test
    void buscarPorNombre_departamentoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> departamentoService.buscarPorNombre("NoExiste"))
                .isInstanceOf(DepartamentoNoEncontradoException.class)
                .hasMessageContaining("Departamento no encontrado con nombre: NoExiste");
    }

    @Test
    void obtenerTodos_conVariosDepartamentos_deberiaRetornarTodos() {
        // Given - Los datos base ya contienen 3 departamentos

        // When
        List<Departamento> todos = departamentoService.obtenerTodos();

        // Then
        assertThat(todos)
                .hasSize(3) // Los 3 departamentos base
                .extracting(Departamento::getNombre)
                .containsExactlyInAnyOrder("Desarrollo", "Marketing", "Recursos Humanos");
    }

    @Test
    void actualizar_departamentoExistente_deberiaActualizarCorrectamente() {
        // Given - Usar departamento base existente
        Departamento actualizado = TestDataFactory.crearDepartamento("Desarrollo Actualizado");
        actualizado.setDescripcion("Nueva descripción del departamento");

        // When
        Departamento resultado = departamentoService.actualizar(DEPARTAMENTO_DESARROLLO_ID, actualizado);

        // Then
        assertThat(resultado)
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(DEPARTAMENTO_DESARROLLO_ID);
                    assertThat(d.getNombre()).isEqualTo("Desarrollo Actualizado");
                    assertThat(d.getDescripcion()).isEqualTo("Nueva descripción del departamento");
                });
    }

    @Test
    void actualizar_departamentoInexistente_deberieLanzarExcepcion() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamento("Test");

        // When & Then
        assertThatThrownBy(() -> departamentoService.actualizar(999L, departamento))
                .isInstanceOf(DepartamentoNoEncontradoException.class)
                .hasMessageContaining("Departamento no encontrado con ID: 999");
    }

    @Test
    void eliminar_departamentoExistente_deberiaEliminarCorrectamente() {
        // Given - Usar departamento base existente
        Long idAEliminar = DEPARTAMENTO_RRHH_ID;

        // When
        departamentoService.eliminar(idAEliminar);

        // Then
        assertThatThrownBy(() -> departamentoService.buscarPorId(idAEliminar))
                .isInstanceOf(DepartamentoNoEncontradoException.class);
    }

    @Test
    void eliminar_departamentoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> departamentoService.eliminar(999L))
                .isInstanceOf(DepartamentoNoEncontradoException.class)
                .hasMessageContaining("Departamento no encontrado con ID: 999");
    }
}
