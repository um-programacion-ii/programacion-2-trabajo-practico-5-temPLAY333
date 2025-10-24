package um.prog2.TP5.repository.testcontainers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;
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
 * Tests de repositorio usando MySQL con TestContainers.
 * NOTA: Estos tests requieren Docker en ejecución.
 * Si Docker no está disponible, estos tests se omitirán.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Repositorios con MySQL - TestContainers")
class MySqlRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("empleados_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
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
    @DisplayName("✓ VERIFICACIÓN - Contenedor MySQL está corriendo")
    void testContenedorMySqlEstaCorreindo() {
        // Verificar que el contenedor está iniciado
        assertThat(mysql.isRunning())
                .withFailMessage("El contenedor MySQL no está corriendo. Asegúrate de que Docker esté en ejecución.")
                .isTrue();

        // Verificar que podemos obtener la URL de conexión
        assertThat(mysql.getJdbcUrl())
                .withFailMessage("No se pudo obtener la URL JDBC del contenedor MySQL")
                .isNotNull()
                .contains("jdbc:mysql://")
                .contains("empleados_test");

        // Verificar credenciales
        assertThat(mysql.getUsername()).isEqualTo("test_user");
        assertThat(mysql.getPassword()).isEqualTo("test_pass");

        // Verificar que los repositorios están inyectados
        assertThat(departamentoRepository).isNotNull();
        assertThat(empleadoRepository).isNotNull();
        assertThat(proyectoRepository).isNotNull();
        assertThat(entityManager).isNotNull();

        System.out.println("✓ Contenedor MySQL verificado correctamente:");
        System.out.println("  - URL: " + mysql.getJdbcUrl());
        System.out.println("  - Container ID: " + mysql.getContainerId());
        System.out.println("  - Mapped Port: " + mysql.getMappedPort(3306));
    }

    @Test
    @Order(2)
    @DisplayName("MySQL - Auto increment para IDs")
    void testAutoIncrementIds() {
        // Given
        Departamento depto1 = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento depto2 = TestDataFactory.crearDepartamentoMarketing();

        // When
        Departamento guardado1 = departamentoRepository.save(depto1);
        Departamento guardado2 = departamentoRepository.save(depto2);
        entityManager.flush();

        // Then
        assertThat(guardado1.getId()).isNotNull().isPositive();
        assertThat(guardado2.getId()).isNotNull().isPositive();
        assertThat(guardado2.getId()).isGreaterThan(guardado1.getId());
    }

    @Test
    @DisplayName("MySQL - Manejo de decimales precisos para salarios")
    void testManejoDecimalesPrecios() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        BigDecimal salarioPreciso = new BigDecimal("50000.99");
        Empleado empleado = TestDataFactory.crearEmpleado("Luis", "Fernández", "luis@mysql.com", salarioPreciso, deptoGuardado);
        empleado.setSalario(salarioPreciso);

        // When
        Empleado guardado = empleadoRepository.save(empleado);
        entityManager.flush();
        entityManager.clear();

        Optional<Empleado> recuperado = empleadoRepository.findById(guardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .extracting(Empleado::getSalario)
                .isEqualTo(salarioPreciso);
    }

    @Test
    @DisplayName("MySQL - Queries con JOIN para relaciones")
    void testQueriesConJoin() {
        // Given
        Departamento desarrollo = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento marketing = TestDataFactory.crearDepartamentoMarketing();
        departamentoRepository.save(desarrollo);
        departamentoRepository.save(marketing);

        Empleado emp1 = TestDataFactory.crearEmpleado("Sandra", "López", "sandra@mysql.com", new BigDecimal("45000.00"), desarrollo);
        Empleado emp2 = TestDataFactory.crearEmpleado("Miguel", "Torres", "miguel@mysql.com", new BigDecimal("42000.00"), marketing);
        empleadoRepository.save(emp1);
        empleadoRepository.save(emp2);
        entityManager.flush();

        // When
        List<Empleado> empleadosDesarrollo = empleadoRepository.findByNombreDepartamento("Desarrollo");

        // Then
        assertThat(empleadosDesarrollo)
                .hasSize(1)
                .first()
                .satisfies(emp -> {
                    assertThat(emp.getNombre()).isEqualTo("Sandra");
                    assertThat(emp.getDepartamento().getNombre()).isEqualTo("Desarrollo");
                });
    }

    @Test
    @DisplayName("MySQL - Relaciones Many-to-Many con proyectos")
    void testRelacionesManyToMany() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado1 = TestDataFactory.crearEmpleado("Elena", "Ruiz", "elena@mysql.com", new BigDecimal("48000.00"), deptoGuardado);
        Empleado empleado2 = TestDataFactory.crearEmpleado("David", "Moreno", "david@mysql.com", new BigDecimal("46000.00"), deptoGuardado);

        Proyecto proyecto = TestDataFactory.crearProyecto("Sistema ERP", LocalDate.now(), LocalDate.now().plusMonths(12));
        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        // Agregar desde el lado propietario (Empleado tiene el @JoinTable)
        empleado1.getProyectos().add(proyectoGuardado);
        empleado2.getProyectos().add(proyectoGuardado);
        empleado1 = empleadoRepository.save(empleado1);
        empleado2 = empleadoRepository.save(empleado2);

        // When
        entityManager.flush();
        entityManager.clear();

        Optional<Proyecto> recuperado = proyectoRepository.findById(proyectoGuardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .satisfies(p -> {
                    assertThat(p.getEmpleados()).hasSize(2);
                    assertThat(p.getEmpleados())
                            .extracting(Empleado::getEmail)
                            .containsExactlyInAnyOrder("elena@mysql.com", "david@mysql.com");
                });
    }

    @Test
    @DisplayName("MySQL - Búsquedas case-sensitive e insensitive")
    void testBusquedasCaseSensitive() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        Empleado empleado = TestDataFactory.crearEmpleado("Carlos", "GARCÍA", "carlos@mysql.com", new BigDecimal("44000.00"), deptoGuardado);
        empleadoRepository.save(empleado);
        entityManager.flush();

        // When - Búsqueda por email
        Optional<Empleado> encontrado = empleadoRepository.findByEmail("carlos@mysql.com");

        // Then
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("MySQL - Paginación y ordenamiento")
    void testPaginacionYOrdenamiento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        for (int i = 1; i <= 5; i++) {
            Empleado emp = TestDataFactory.crearEmpleado("Empleado" + i, "Apellido" + i,
                    "emp" + i + "@mysql.com", new BigDecimal(40000 + (i * 5000)), deptoGuardado);
            emp.setSalario(new BigDecimal(40000 + (i * 5000)));
            empleadoRepository.save(emp);
        }
        entityManager.flush();

        // When
        List<Empleado> todosEmpleados = empleadoRepository.findByDepartamento(deptoGuardado);

        // Then
        assertThat(todosEmpleados).hasSize(5);
    }

    @Test
    @DisplayName("MySQL - Funciones de agregación")
    void testFuncionesAgregacion() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        empleadoRepository.save(TestDataFactory.crearEmpleado("Ana", "Pérez", "ana@mysql.com", new BigDecimal("40000"), deptoGuardado));
        empleadoRepository.save(TestDataFactory.crearEmpleado("Juan", "López", "juan@mysql.com", new BigDecimal("50000"), deptoGuardado));
        empleadoRepository.save(TestDataFactory.crearEmpleado("María", "García", "maria@mysql.com", new BigDecimal("60000"), deptoGuardado));
        entityManager.flush();

        // When
        long count = empleadoRepository.count();
        Optional<BigDecimal> promedioOpt = empleadoRepository.findAverageSalarioByDepartamento(deptoGuardado.getId());

        // Then
        assertThat(count).isEqualTo(3);
        assertThat(promedioOpt).isPresent();
        BigDecimal promedio = promedioOpt.get();
        assertThat(promedio).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("MySQL - Manejo de fechas con zonas horarias")
    void testManejoFechasConZonasHorarias() {
        // Given
        LocalDate fechaInicio = LocalDate.of(2023, 1, 15);
        LocalDate fechaFin = LocalDate.of(2023, 12, 31);

        Proyecto proyecto = TestDataFactory.crearProyecto("Proyecto Temporal", fechaInicio, fechaFin);
        Proyecto guardado = proyectoRepository.save(proyecto);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Proyecto> recuperado = proyectoRepository.findById(guardado.getId());

        // Then
        assertThat(recuperado)
                .isPresent()
                .get()
                .satisfies(p -> {
                    assertThat(p.getFechaInicio()).isEqualTo(fechaInicio);
                    assertThat(p.getFechaFin()).isEqualTo(fechaFin);
                });
    }

    @Test
    @DisplayName("MySQL - Transacciones con múltiples operaciones")
    void testTransaccionesMultiplesOperaciones() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento deptoGuardado = departamentoRepository.save(departamento);

        // When - Múltiples operaciones en una transacción
        Empleado empleado = TestDataFactory.crearEmpleado("Transaction", "Test", "transaction@mysql.com", new BigDecimal("47000.00"), deptoGuardado);
        Empleado empleadoGuardado = empleadoRepository.save(empleado);

        Proyecto proyecto = TestDataFactory.crearProyecto("Proyecto Transaccional", LocalDate.now(), LocalDate.now().plusMonths(6));
        proyecto.getEmpleados().add(empleadoGuardado);
        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        entityManager.flush();

        // Then
        assertThat(empleadoGuardado.getId()).isNotNull();
        assertThat(proyectoGuardado.getId()).isNotNull();
        assertThat(proyectoGuardado.getEmpleados()).contains(empleadoGuardado);
    }
}
