package um.prog2.TP5.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.exception.EmailDuplicadoException;
import um.prog2.TP5.exception.EmpleadoNoEncontradoException;
import um.prog2.TP5.test.base.BaseIntegrationTest;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para EmpleadoService
 * Extiende BaseIntegrationTest para obtener limpieza automática y utilidades
 */
class EmpleadoServiceTest extends BaseIntegrationTest {

    @Autowired
    private EmpleadoService empleadoService;

    @BeforeEach
    void configurarDatosBase() {
        // Crear empleados base conectados con departamentos y proyectos
        // Esto proporciona un entorno completo para testing
        crearEmpleadosConProyectos();
    }

    @Test
    void guardar_empleadoValido_deberiaValidarEmpleadoCorrectamente() {
        // Given
        Departamento departamento = departamentoRepository.findById(DEPARTAMENTO_DESARROLLO_ID).orElseThrow();
        Empleado empleado = TestDataFactory.crearEmpleado("Nuevo", "Empleado", "nuevo@empresa.com",
                                                         new BigDecimal("55000"), departamento);

        // When
        Empleado guardado = empleadoService.validarEmpleado(empleado);

        // Then
        assertThat(guardado)
                .isNotNull()
                .satisfies(e -> {
                    assertThat(e.getId()).isNotNull();
                    assertThat(e.getNombre()).isEqualTo("Nuevo");
                    assertThat(e.getApellido()).isEqualTo("Empleado");
                    assertThat(e.getEmail()).isEqualTo("nuevo@empresa.com");
                    assertThat(e.getSalario()).isEqualTo(new BigDecimal("55000"));
                    assertThat(e.getDepartamento().getId()).isEqualTo(DEPARTAMENTO_DESARROLLO_ID);
                });
    }

    @Test
    void validarEmpleado_empleadoConEmailDuplicado_deberieLanzarExcepcion() {
        // Given - Intentar crear empleado con email que ya existe
        Departamento departamento = departamentoRepository.findById(DEPARTAMENTO_MARKETING_ID).orElseThrow();
        Empleado empleadoDuplicado = TestDataFactory.crearEmpleado("Otro", "Nombre", "juan.perez@empresa.com",
                                                                  new BigDecimal("60000"), departamento);

        // When & Then
        assertThatThrownBy(() -> empleadoService.validarEmpleado(empleadoDuplicado))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessageContaining("El email ya está registrado: juan.perez@empresa.com");
    }

    @Test
    void buscarPorId_empleadoExistente_deberiaRetornarEmpleado() {
        // Given - Usar datos base en lugar de crear nuevos
        // Ya tenemos EMPLEADO_DESARROLLADOR_ID creado en @BeforeEach

        // When
        Empleado encontrado = empleadoService.buscarPorId(EMPLEADO_DESARROLLADOR_ID);

        // Then
        assertThat(encontrado)
                .isNotNull()
                .satisfies(e -> {
                    assertThat(e.getNombre()).isEqualTo("Juan");
                    assertThat(e.getApellido()).isEqualTo("Pérez");
                    assertThat(e.getEmail()).isEqualTo("juan.perez@empresa.com");
                    assertThat(e.getDepartamento().getNombre()).isEqualTo("Desarrollo");
                });
    }

    @Test
    void buscarPorId_empleadoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> empleadoService.buscarPorId(999L))
                .isInstanceOf(EmpleadoNoEncontradoException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");
    }

    @Test
    void buscarPorDepartamento_conEmpleadosEnDepartamento_deberiaRetornarCorrectamente() {
        // Given - Los datos base ya contienen empleados en departamento Desarrollo

        // When
        List<Empleado> empleadosDesarrollo = empleadoService.buscarPorDepartamento("Desarrollo");

        // Then
        assertThat(empleadosDesarrollo)
                .hasSize(3) // Desarrollador, Senior, Junior
                .extracting(Empleado::getNombre)
                .containsExactlyInAnyOrder("Juan", "María", "Carlos");
    }

    @Test
    void buscarPorDepartamento_departamentoSinEmpleados_deberiaRetornarListaVacia() {
        // When
        List<Empleado> empleados = empleadoService.buscarPorDepartamento("NoExiste");

        // Then
        assertThat(empleados).isEmpty();
    }

    @Test
    void buscarPorRangoSalario_conEmpleadosEnRango_deberiaRetornarCorrectamente() {
        // Given - Los datos base contienen empleados con diferentes salarios
        // Junior: 45000, Desarrollador: 75000, Senior: 95000, Marketing: 60000, RRHH: 65000
        // Marketing Senior: 70000, Marketing Junior: 50000, RRHH Senior: 72000, RRHH Junior: 48000

        // When - Buscar empleados con salario entre 60000 y 80000
        List<Empleado> empleadosEnRango = empleadoService.buscarPorRangoSalario(
                new BigDecimal("60000"), new BigDecimal("80000"));

        // Then
        assertThat(empleadosEnRango)
                .hasSize(5) // Marketing: 60000, RRHH: 65000, Desarrollador: 75000, Marketing Senior: 70000, RRHH Senior: 72000
                .extracting(Empleado::getNombre)
                .containsExactlyInAnyOrder("Juan", "Ana", "Pedro", "Laura", "Carmen");
    }

    @Test
    void obtenerSalarioPromedioPorDepartamento_conEmpleados_deberiaCalcularCorrectamente() {
        // Given - Departamento Desarrollo tiene: Junior (45000), Desarrollador (75000), Senior (95000)
        // Promedio: (45000 + 75000 + 95000) / 3 = 71666.66666...

        // When
        BigDecimal promedio = empleadoService.obtenerSalarioPromedioPorDepartamento(DEPARTAMENTO_DESARROLLO_ID);

        // Then
        // Redondear a 2 decimales para la comparación
        assertThat(promedio.setScale(2, BigDecimal.ROUND_HALF_UP))
                .isEqualTo(new BigDecimal("71666.67"));
    }

    @Test
    void obtenerSalarioPromedioPorDepartamento_sinEmpleados_deberiaRetornarCero() {
        // Given - Crear departamento sin empleados
        Departamento depVacio = TestDataFactory.crearDepartamento("Departamento Vacío");
        Long depVacioId = departamentoRepository.save(depVacio).getId();

        // When
        BigDecimal promedio = empleadoService.obtenerSalarioPromedioPorDepartamento(depVacioId);

        // Then
        assertThat(promedio).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void obtenerTodos_conVariosEmpleados_deberiaRetornarTodos() {
        // Given - Los datos base ya contienen 9 empleados (3 por departamento)

        // When
        List<Empleado> todos = empleadoService.obtenerTodos();

        // Then
        assertThat(todos)
                .hasSize(9) // Los 9 empleados base
                .extracting(Empleado::getNombre)
                .containsExactlyInAnyOrder("Juan", "María", "Carlos", "Ana", "Laura", "Diego", "Pedro", "Carmen", "Roberto");
    }

    @Test
    void actualizar_empleadoExistente_deberiaActualizarCorrectamente() {
        // Given - Usar empleado base existente
        Departamento nuevoDepto = departamentoRepository.findById(DEPARTAMENTO_MARKETING_ID).orElseThrow();
        Empleado actualizado = TestDataFactory.crearEmpleado("Juan Actualizado", "Pérez Actualizado",
                                                            "juan.actualizado@empresa.com",
                                                            new BigDecimal("85000"), nuevoDepto);

        // When
        Empleado resultado = empleadoService.actualizar(EMPLEADO_DESARROLLADOR_ID, actualizado);

        // Then
        assertThat(resultado)
                .satisfies(e -> {
                    assertThat(e.getId()).isEqualTo(EMPLEADO_DESARROLLADOR_ID);
                    assertThat(e.getNombre()).isEqualTo("Juan Actualizado");
                    assertThat(e.getApellido()).isEqualTo("Pérez Actualizado");
                    assertThat(e.getEmail()).isEqualTo("juan.actualizado@empresa.com");
                    assertThat(e.getSalario()).isEqualTo(new BigDecimal("85000"));
                    assertThat(e.getDepartamento().getId()).isEqualTo(DEPARTAMENTO_MARKETING_ID);
                });
    }

    @Test
    void actualizar_empleadoInexistente_deberieLanzarExcepcion() {
        // Given
        Departamento departamento = departamentoRepository.findById(DEPARTAMENTO_DESARROLLO_ID).orElseThrow();
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamento);

        // When & Then
        assertThatThrownBy(() -> empleadoService.actualizar(999L, empleado))
                .isInstanceOf(EmpleadoNoEncontradoException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");
    }

    @Test
    void eliminar_empleadoExistente_deberiaEliminarCorrectamente() {
        // Given - Usar empleado base existente
        Long idAEliminar = EMPLEADO_JUNIOR_ID;

        // When
        empleadoService.eliminar(idAEliminar);

        // Then
        assertThatThrownBy(() -> empleadoService.buscarPorId(idAEliminar))
                .isInstanceOf(EmpleadoNoEncontradoException.class);
    }

    @Test
    void eliminar_empleadoInexistente_deberieLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> empleadoService.eliminar(999L))
                .isInstanceOf(EmpleadoNoEncontradoException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");
    }

    @Test
    void empleadosConProyectos_deberianTenerRelacionesConfiguradas() {
        // Given - Los datos base incluyen relaciones ManyToMany más complejas con proyectos

        // When
        Empleado desarrollador = empleadoService.buscarPorId(EMPLEADO_DESARROLLADOR_ID);
        Empleado senior = empleadoService.buscarPorId(EMPLEADO_SENIOR_ID);
        Empleado junior = empleadoService.buscarPorId(EMPLEADO_JUNIOR_ID);
        Empleado marketing = empleadoService.buscarPorId(EMPLEADO_MARKETING_ID);
        Empleado marketingSenior = empleadoService.buscarPorId(EMPLEADO_MARKETING_SENIOR_ID);
        Empleado rrhhSenior = empleadoService.buscarPorId(EMPLEADO_RRHH_SENIOR_ID);

        // Then
        // Senior Developer - trabaja en todos los proyectos (el más experimentado)
        assertThat(senior.getProyectos())
                .hasSize(3)
                .extracting("nombre")
                .containsExactlyInAnyOrder("Proyecto Base Activo", "Proyecto Base Sin Fin", "Proyecto Base Finalizado");

        // Desarrollador - trabaja en proyecto activo y sin fin
        assertThat(desarrollador.getProyectos())
                .hasSize(2)
                .extracting("nombre")
                .containsExactlyInAnyOrder("Proyecto Base Activo", "Proyecto Base Sin Fin");

        // Junior Developer - solo en proyecto activo (aprendiendo)
        assertThat(junior.getProyectos())
                .hasSize(1)
                .extracting("nombre")
                .containsExactly("Proyecto Base Activo");

        // Marketing Senior - en proyecto activo y proyecto finalizado
        assertThat(marketingSenior.getProyectos())
                .hasSize(2)
                .extracting("nombre")
                .containsExactlyInAnyOrder("Proyecto Base Activo", "Proyecto Base Finalizado");

        // Marketing Regular - solo en proyecto sin fin
        assertThat(marketing.getProyectos())
                .hasSize(1)
                .extracting("nombre")
                .containsExactly("Proyecto Base Sin Fin");

        // RRHH Senior - en proyecto activo (para temas de personal)
        assertThat(rrhhSenior.getProyectos())
                .hasSize(1)
                .extracting("nombre")
                .containsExactly("Proyecto Base Activo");
    }
}
