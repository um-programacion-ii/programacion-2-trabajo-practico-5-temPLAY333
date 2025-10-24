package um.prog2.TP5.repository.testcontainers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.repository.DepartamentoRepository;
import um.prog2.TP5.repository.EmpleadoRepository;
import um.prog2.TP5.repository.ProyectoRepository;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de repositorio usando PostgreSQL con TestContainers.
 * Estos tests prueban la funcionalidad de los repositorios contra una instancia real
 * de PostgreSQL ejecutándose en un contenedor Docker.
 * NOTA: Estos tests requieren Docker en ejecución.
 */

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Repositorios con PostgreSQL - TestContainers")
class PostgreSqlRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("empleados_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        empleadoRepository.deleteAll();
        proyectoRepository.deleteAll();
        departamentoRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Order(1)
    @DisplayName("✓ VERIFICACIÓN - Contenedor PostgreSQL está corriendo")
    void testContenedorPostgreSqlEstaCorreindo() {
        // Verificar que el contenedor está iniciado
        assertThat(postgres.isRunning())
                .withFailMessage("El contenedor PostgreSQL no está corriendo. Asegúrate de que Docker esté en ejecución.")
                .isTrue();

        // Verificar que podemos obtener la URL de conexión
        assertThat(postgres.getJdbcUrl())
                .withFailMessage("No se pudo obtener la URL JDBC del contenedor PostgreSQL")
                .isNotNull()
                .contains("jdbc:postgresql://")
                .contains("empleados_test");

        // Verificar credenciales
        assertThat(postgres.getUsername()).isEqualTo("test_user");
        assertThat(postgres.getPassword()).isEqualTo("test_pass");

        // Verificar que los repositorios están inyectados
        assertThat(departamentoRepository).isNotNull();
        assertThat(empleadoRepository).isNotNull();
        assertThat(proyectoRepository).isNotNull();
        assertThat(entityManager).isNotNull();

        System.out.println("✓ Contenedor PostgreSQL verificado correctamente:");
        System.out.println("  - URL: " + postgres.getJdbcUrl());
        System.out.println("  - Container ID: " + postgres.getContainerId());
        System.out.println("  - Mapped Port: " + postgres.getMappedPort(5432));
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL - Guardar y recuperar departamento")
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
    @Order(3)
    @DisplayName("PostgreSQL - Buscar departamento por nombre")
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
    @Order(4)
    @DisplayName("PostgreSQL - Relación entre empleado y departamento")
    void testRelacionEmpleadoDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("Juan", "Pérez", "juan@postgres.com", new BigDecimal("45000.00"), deptoGuardado);
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
                    assertThat(emp.getEmail()).isEqualTo("juan@postgres.com");
                    assertThat(emp.getDepartamento()).isNotNull();
                    assertThat(emp.getDepartamento().getNombre()).isEqualTo("Desarrollo");
                });
    }

    @Test
    @Order(5)
    @DisplayName("PostgreSQL - Búsqueda de empleados por departamento")
    void testBuscarEmpleadosPorDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado emp1 = TestDataFactory.crearEmpleado("Ana", "García", "ana@postgres.com", new BigDecimal("42000.00"), deptoGuardado);
        Empleado emp2 = TestDataFactory.crearEmpleado("Carlos", "López", "carlos@postgres.com", new BigDecimal("48000.00"), deptoGuardado);

        empleadoRepository.save(emp1);
        empleadoRepository.save(emp2);
        entityManager.flush();

        // When
        List<Empleado> empleados = empleadoRepository.findByNombreDepartamento("Desarrollo");

        // Then
        assertThat(empleados)
                .hasSize(2)
                .extracting(Empleado::getEmail)
                .containsExactlyInAnyOrder("ana@postgres.com", "carlos@postgres.com");
    }

    @Test
    @Order(6)
    @DisplayName("PostgreSQL - Cálculo de salario promedio")
    void testCalculoSalarioPromedio() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado emp1 = TestDataFactory.crearEmpleado("Pedro", "Martín", "pedro@postgres.com", new BigDecimal("50000.00"), deptoGuardado);
        emp1.setSalario(new BigDecimal("50000.00"));

        Empleado emp2 = TestDataFactory.crearEmpleado("Laura", "Silva", "laura@postgres.com", new BigDecimal("60000.00"), deptoGuardado);
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
    @Order(7)
    @DisplayName("PostgreSQL - Búsqueda por email con índice único")
    void testBusquedaPorEmailConIndiceUnico() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("María", "Rodríguez", "maria.unique@postgres.com", new BigDecimal("40000.00"), deptoGuardado);
        empleadoRepository.save(empleado);
        entityManager.flush();

        // When
        Optional<Empleado> encontrado = empleadoRepository.findByEmail("maria.unique@postgres.com");

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .satisfies(emp -> {
                    assertThat(emp.getNombre()).isEqualTo("María");
                    assertThat(emp.getApellido()).isEqualTo("Rodríguez");
                });
    }

    @Test
    @Order(8)
    @DisplayName("PostgreSQL - Persistencia de fechas")
    void testPersistenciaFechas() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        LocalDate fechaContratacion = LocalDate.of(2023, 6, 15);
        Empleado empleado = TestDataFactory.crearEmpleado("Roberto", "Torres", "roberto@postgres.com", new BigDecimal("38000.00"), deptoGuardado);
        empleado.setFechaContratacion(fechaContratacion);

        Empleado guardado = empleadoRepository.save(empleado);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Empleado> recuperado = empleadoRepository.findById(guardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .extracting(Empleado::getFechaContratacion)
                .isEqualTo(fechaContratacion);
    }

    @Test
    @Order(9)
    @DisplayName("PostgreSQL - Constraint de email único")
    void testConstraintEmailUnico() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado1 = TestDataFactory.crearEmpleado("Test1", "User1", "unique@postgres.com", new BigDecimal("35000.00"), deptoGuardado);
        empleadoRepository.save(empleado1);
        entityManager.flush();
        entityManager.clear();

        // When & Then - Intentar guardar otro empleado con el mismo email debe fallar
        boolean excepcionCapturada = false;
        try {
            Empleado empleado2 = TestDataFactory.crearEmpleado("Test2", "User2", "unique@postgres.com", new BigDecimal("36000.00"), deptoGuardado);
            empleadoRepository.save(empleado2);
            entityManager.flush();
        } catch (Exception e) {
            // Se espera una excepción por violación de constraint de email único
            excepcionCapturada = true;
        }

        // Verificar que se lanzó la excepción esperada
        assertThat(excepcionCapturada)
                .withFailMessage("Se esperaba una excepción por violación de constraint de email único")
                .isTrue();
    }
}

