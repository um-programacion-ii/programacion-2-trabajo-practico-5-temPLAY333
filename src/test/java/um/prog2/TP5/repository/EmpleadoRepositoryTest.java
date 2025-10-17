package um.prog2.TP5.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para EmpleadoRepository usando @DataJpaTest para mayor velocidad
 */
@DataJpaTest
@ActiveProfiles("test")
class EmpleadoRepositoryTest {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_empleadoExistente_deberiaRetornarEmpleado() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        entityManager.persist(departamento);

        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamento);
        entityManager.persistAndFlush(empleado);

        // When
        Optional<Empleado> encontrado = empleadoRepository.findByEmail("juan.perez@empresa.com");

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .satisfies(e -> {
                    assertThat(e.getNombre()).isEqualTo("Juan");
                    assertThat(e.getApellido()).isEqualTo("Pérez");
                    assertThat(e.getEmail()).isEqualTo("juan.perez@empresa.com");
                    assertThat(e.getSalario()).isEqualTo(new BigDecimal("75000.00"));
                });
    }

    @Test
    void findByEmail_empleadoInexistente_deberiaRetornarEmpty() {
        // When
        Optional<Empleado> encontrado = empleadoRepository.findByEmail("noexiste@empresa.com");

        // Then
        assertThat(encontrado).isEmpty();
    }

    @Test
    void findBySalarioBetween_conEmpleadosEnRango_deberiaRetornarCorrectamente() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        entityManager.persist(departamento);

        Empleado junior = TestDataFactory.crearEmpleadoJunior(departamento); // 45000
        Empleado desarrollador = TestDataFactory.crearEmpleadoDesarrollador(departamento); // 75000
        Empleado senior = TestDataFactory.crearEmpleadoSenior(departamento); // 95000

        entityManager.persist(junior);
        entityManager.persist(desarrollador);
        entityManager.persist(senior);
        entityManager.flush();

        // When - Buscar empleados con salario entre 50000 y 80000
        List<Empleado> empleadosEnRango = empleadoRepository.findBySalarioBetween(
                new BigDecimal("50000"), new BigDecimal("80000"));

        // Then
        assertThat(empleadosEnRango)
                .hasSize(1)
                .extracting(Empleado::getNombre)
                .containsExactly("Juan"); // Solo el desarrollador (75000)
    }

    @Test
    void findByNombreDepartamento_conEmpleadosEnDepartamento_deberiaRetornarCorrectamente() {
        // Given
        Departamento desarrollo = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento marketing = TestDataFactory.crearDepartamentoMarketing();
        entityManager.persist(desarrollo);
        entityManager.persist(marketing);

        Empleado empDesarrollo1 = TestDataFactory.crearEmpleadoDesarrollador(desarrollo);
        Empleado empDesarrollo2 = TestDataFactory.crearEmpleadoSenior(desarrollo);
        Empleado empMarketing = TestDataFactory.crearEmpleadoMarketing(marketing);

        entityManager.persist(empDesarrollo1);
        entityManager.persist(empDesarrollo2);
        entityManager.persist(empMarketing);
        entityManager.flush();

        // When
        List<Empleado> empleadosDesarrollo = empleadoRepository.findByNombreDepartamento("Desarrollo");

        // Then
        assertThat(empleadosDesarrollo)
                .hasSize(2)
                .extracting(Empleado::getNombre)
                .containsExactlyInAnyOrder("Juan", "María");
    }

    @Test
    void findByNombreDepartamento_departamentoSinEmpleados_deberiaRetornarListaVacia() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        entityManager.persistAndFlush(departamento);

        // When
        List<Empleado> empleados = empleadoRepository.findByNombreDepartamento("Desarrollo");

        // Then
        assertThat(empleados).isEmpty();
    }

    @Test
    void save_empleadoValido_deberiaGuardarCorrectamente() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        entityManager.persist(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("Test", "Usuario", "test@empresa.com",
                                                         new BigDecimal("50000"), departamento);

        // When
        Empleado guardado = empleadoRepository.save(empleado);

        // Then
        assertThat(guardado)
                .isNotNull()
                .satisfies(e -> {
                    assertThat(e.getId()).isNotNull();
                    assertThat(e.getNombre()).isEqualTo("Test");
                    assertThat(e.getApellido()).isEqualTo("Usuario");
                    assertThat(e.getEmail()).isEqualTo("test@empresa.com");
                    assertThat(e.getDepartamento().getNombre()).isEqualTo("Desarrollo");
                });
    }
}
