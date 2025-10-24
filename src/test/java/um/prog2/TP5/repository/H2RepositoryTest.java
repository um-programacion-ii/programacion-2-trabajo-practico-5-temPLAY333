package um.prog2.TP5.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Tests de repositorio usando H2 en memoria.
 * Estos tests prueban la funcionalidad de los repositorios contra una base de datos H2 en memoria,
 * lo que permite pruebas más rápidas sin necesidad de Docker.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repositorios con H2 - Tests unitarios")
class H2RepositoryTest {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        empleadoRepository.deleteAll();
        departamentoRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("H2 - Guardar y recuperar departamento")
    void testGuardarYRecuperarDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();

        // When
        Departamento guardado = departamentoRepository.save(departamento);
        entityManager.flush();
        entityManager.clear();

        Optional<Departamento> recuperado = departamentoRepository.findById(guardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(guardado.getId());
                    assertThat(d.getNombre()).isEqualTo("Desarrollo");
                    assertThat(d.getDescripcion()).isEqualTo("Descripción del departamento Desarrollo");
                });
    }

    @Test
    @DisplayName("H2 - Buscar departamento por nombre")
    void testBuscarDepartamentoPorNombre() {
        // Given
        Departamento marketing = TestDataFactory.crearDepartamentoMarketing();
        Departamento ventas = TestDataFactory.crearDepartamentoVentas();
        departamentoRepository.save(marketing);
        departamentoRepository.save(ventas);
        entityManager.flush();

        // When
        Optional<Departamento> encontrado = departamentoRepository.findByNombre("Marketing");

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .extracting(Departamento::getNombre)
                .isEqualTo("Marketing");
    }

    @Test
    @DisplayName("H2 - Relación entre empleado y departamento")
    void testRelacionEmpleadoDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@h2.com", new BigDecimal("45000.00"), deptoGuardado);
        Empleado empleadoGuardado = empleadoRepository.save(empleado);

        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Empleado> recuperado = empleadoRepository.findById(empleadoGuardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .satisfies(emp -> {
                    assertThat(emp.getNombre()).isEqualTo("Juan");
                    assertThat(emp.getEmail()).isEqualTo("juan@h2.com");
                    assertThat(emp.getDepartamento()).isNotNull();
                    assertThat(emp.getDepartamento().getNombre()).isEqualTo("Desarrollo");
                });
    }

    @Test
    @DisplayName("H2 - Búsqueda de empleados por departamento")
    void testBuscarEmpleadosPorDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado emp1 = TestDataFactory.crearEmpleado("Ana", "García", "ana@h2.com", new BigDecimal("42000.00"), deptoGuardado);
        Empleado emp2 = TestDataFactory.crearEmpleado("Carlos", "López", "carlos@h2.com", new BigDecimal("48000.00"), deptoGuardado);

        empleadoRepository.save(emp1);
        empleadoRepository.save(emp2);
        entityManager.flush();

        // When
        List<Empleado> empleados = empleadoRepository.findByNombreDepartamento("Desarrollo");

        // Then
        assertThat(empleados)
                .hasSize(2)
                .extracting(Empleado::getEmail)
                .containsExactlyInAnyOrder("ana@h2.com", "carlos@h2.com");
    }

    @Test
    @DisplayName("H2 - Cálculo de salario promedio")
    void testCalculoSalarioPromedio() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado emp1 = TestDataFactory.crearEmpleado("Pedro", "Martín", "pedro@h2.com", new BigDecimal("50000.00"), deptoGuardado);
        emp1.setSalario(new BigDecimal("50000.00"));

        Empleado emp2 = TestDataFactory.crearEmpleado("Laura", "Silva", "laura@h2.com", new BigDecimal("60000.00"), deptoGuardado);
        emp2.setSalario(new BigDecimal("60000.00"));

        empleadoRepository.save(emp1);
        empleadoRepository.save(emp2);
        entityManager.flush();

        // When
        Optional<BigDecimal> promedioSalarioOpt = empleadoRepository.findAverageSalarioByDepartamento(deptoGuardado.getId());

        // Then
        assertThat(promedioSalarioOpt)
                .isPresent();

        BigDecimal promedioSalario = promedioSalarioOpt.get();
        assertThat(promedioSalario)
                .isNotNull()
                .isEqualByComparingTo(new BigDecimal("55000.00"));
    }

    @Test
    @DisplayName("H2 - Búsqueda por email con índice único")
    void testBusquedaPorEmailConIndiceUnico() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("María", "Rodríguez", "maria.unique@h2.com", new BigDecimal("40000.00"), deptoGuardado);
        empleadoRepository.save(empleado);
        entityManager.flush();

        // When
        Optional<Empleado> encontrado = empleadoRepository.findByEmail("maria.unique@h2.com");

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .satisfies(emp -> {
                    assertThat(emp.getNombre()).isEqualTo("María");
                    assertThat(emp.getApellido()).isEqualTo("Rodríguez");
                });
    }
}
