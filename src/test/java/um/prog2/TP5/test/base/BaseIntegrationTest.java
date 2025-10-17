package um.prog2.TP5.test.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.repository.DepartamentoRepository;
import um.prog2.TP5.repository.EmpleadoRepository;
import um.prog2.TP5.repository.ProyectoRepository;
import um.prog2.TP5.testutil.TestDataFactory;

/**
 * Clase base abstracta para tests de integración
 * Proporciona limpieza automática y datos de prueba consistentes
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ProyectoRepository proyectoRepository;

    @Autowired
    protected DepartamentoRepository departamentoRepository;

    @Autowired
    protected EmpleadoRepository empleadoRepository;

    // IDs predecibles para los datos base - Proyectos
    protected Long PROYECTO_ACTIVO_ID;
    protected Long PROYECTO_FINALIZADO_ID;
    protected Long PROYECTO_SIN_FIN_ID;

    // IDs predecibles para los datos base - Departamentos
    protected Long DEPARTAMENTO_DESARROLLO_ID;
    protected Long DEPARTAMENTO_MARKETING_ID;
    protected Long DEPARTAMENTO_RRHH_ID;

    // IDs predecibles para los datos base - Empleados (actualizados)
    protected Long EMPLEADO_DESARROLLADOR_ID;
    protected Long EMPLEADO_SENIOR_ID;
    protected Long EMPLEADO_JUNIOR_ID;
    protected Long EMPLEADO_MARKETING_ID;
    protected Long EMPLEADO_MARKETING_SENIOR_ID;
    protected Long EMPLEADO_MARKETING_JUNIOR_ID;
    protected Long EMPLEADO_RRHH_ID;
    protected Long EMPLEADO_RRHH_SENIOR_ID;
    protected Long EMPLEADO_RRHH_JUNIOR_ID;

    @BeforeEach
    void limpiarBaseDeDatos() {
        // Limpieza selectiva - orden importante por foreign keys
        jdbcTemplate.execute("DELETE FROM empleado_proyecto"); // Tabla de relación ManyToMany
        jdbcTemplate.execute("DELETE FROM empleados");
        jdbcTemplate.execute("DELETE FROM proyectos");
        jdbcTemplate.execute("DELETE FROM departamentos");

        // No es necesario reiniciar secuencias en H2 con @Transactional
        // H2 usa AUTO_INCREMENT y los tests hacen rollback automático
    }

    /**
     * Helper para crear datos iniciales comunes si es necesario
     * Este método puede ser sobrescrito por las clases hijas
     */
    protected void crearDatosIniciales() {
        // Método que pueden sobrescribir las clases hijas si necesitan datos base
    }

    /**
     * Crea un conjunto estándar de proyectos para testing
     * Útil para tests de servicio donde necesitas datos pre-existentes
     */
    protected void crearProyectosBase() {
        Proyecto proyectoActivo = TestDataFactory.crearProyectoActivo("Proyecto Base Activo");
        Proyecto proyectoFinalizado = TestDataFactory.crearProyectoFinalizado("Proyecto Base Finalizado");
        Proyecto proyectoSinFin = TestDataFactory.crearProyectoSinFechaFin("Proyecto Base Sin Fin");

        PROYECTO_ACTIVO_ID = proyectoRepository.save(proyectoActivo).getId();
        PROYECTO_FINALIZADO_ID = proyectoRepository.save(proyectoFinalizado).getId();
        PROYECTO_SIN_FIN_ID = proyectoRepository.save(proyectoSinFin).getId();
    }

    /**
     * Crea un conjunto estándar de departamentos para testing
     * Útil para tests de servicio donde necesitas datos pre-existentes
     */
    protected void crearDepartamentosBase() {
        Departamento desarrollo = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento marketing = TestDataFactory.crearDepartamentoMarketing();
        Departamento rrhh = TestDataFactory.crearDepartamentoRecursosHumanos();

        DEPARTAMENTO_DESARROLLO_ID = departamentoRepository.save(desarrollo).getId();
        DEPARTAMENTO_MARKETING_ID = departamentoRepository.save(marketing).getId();
        DEPARTAMENTO_RRHH_ID = departamentoRepository.save(rrhh).getId();
    }

    /**
     * Crea un conjunto estándar de empleados para testing
     * Útil para tests de servicio donde necesitas datos pre-existentes
     * Los empleados se conectan con los departamentos ya creados
     * Ahora cada departamento tiene múltiples empleados
     */
    protected void crearEmpleadosBase() {
        // Primero crear departamentos si no existen
        if (DEPARTAMENTO_DESARROLLO_ID == null) {
            crearDepartamentosBase();
        }

        Departamento depDesarrollo = departamentoRepository.findById(DEPARTAMENTO_DESARROLLO_ID).orElseThrow();
        Departamento depMarketing = departamentoRepository.findById(DEPARTAMENTO_MARKETING_ID).orElseThrow();
        Departamento depRRHH = departamentoRepository.findById(DEPARTAMENTO_RRHH_ID).orElseThrow();

        // Empleados de Desarrollo (3 empleados)
        Empleado desarrollador = TestDataFactory.crearEmpleadoDesarrollador(depDesarrollo);
        Empleado senior = TestDataFactory.crearEmpleadoSenior(depDesarrollo);
        Empleado junior = TestDataFactory.crearEmpleadoJunior(depDesarrollo);

        // Empleados de Marketing (3 empleados)
        Empleado marketing = TestDataFactory.crearEmpleadoMarketing(depMarketing);
        Empleado marketingSenior = TestDataFactory.crearEmpleadoMarketingSenior(depMarketing);
        Empleado marketingJunior = TestDataFactory.crearEmpleadoMarketingJunior(depMarketing);

        // Empleados de RRHH (3 empleados)
        Empleado rrhh = TestDataFactory.crearEmpleadoRRHH(depRRHH);
        Empleado rrhhSenior = TestDataFactory.crearEmpleadoRRHHSenior(depRRHH);
        Empleado rrhhJunior = TestDataFactory.crearEmpleadoRRHHJunior(depRRHH);

        EMPLEADO_DESARROLLADOR_ID = empleadoRepository.save(desarrollador).getId();
        EMPLEADO_SENIOR_ID = empleadoRepository.save(senior).getId();
        EMPLEADO_JUNIOR_ID = empleadoRepository.save(junior).getId();
        EMPLEADO_MARKETING_ID = empleadoRepository.save(marketing).getId();
        EMPLEADO_MARKETING_SENIOR_ID = empleadoRepository.save(marketingSenior).getId();
        EMPLEADO_MARKETING_JUNIOR_ID = empleadoRepository.save(marketingJunior).getId();
        EMPLEADO_RRHH_ID = empleadoRepository.save(rrhh).getId();
        EMPLEADO_RRHH_SENIOR_ID = empleadoRepository.save(rrhhSenior).getId();
        EMPLEADO_RRHH_JUNIOR_ID = empleadoRepository.save(rrhhJunior).getId();
    }

    /**
     * Crea empleados base y los asigna a proyectos para testing completo
     * Útil para tests que necesitan probar relaciones ManyToMany
     * Configura empleados trabajando en múltiples proyectos simultáneamente
     */
    protected void crearEmpleadosConProyectos() {
        // Crear empleados base
        crearEmpleadosBase();

        // Crear proyectos si no existen
        if (PROYECTO_ACTIVO_ID == null) {
            crearProyectosBase();
        }

        // Cargar empleados y proyectos
        Empleado desarrollador = empleadoRepository.findById(EMPLEADO_DESARROLLADOR_ID).orElseThrow();
        Empleado senior = empleadoRepository.findById(EMPLEADO_SENIOR_ID).orElseThrow();
        Empleado junior = empleadoRepository.findById(EMPLEADO_JUNIOR_ID).orElseThrow();
        Empleado marketing = empleadoRepository.findById(EMPLEADO_MARKETING_ID).orElseThrow();
        Empleado marketingSenior = empleadoRepository.findById(EMPLEADO_MARKETING_SENIOR_ID).orElseThrow();
        Empleado rrhhSenior = empleadoRepository.findById(EMPLEADO_RRHH_SENIOR_ID).orElseThrow();

        Proyecto proyectoActivo = proyectoRepository.findById(PROYECTO_ACTIVO_ID).orElseThrow();
        Proyecto proyectoSinFin = proyectoRepository.findById(PROYECTO_SIN_FIN_ID).orElseThrow();
        Proyecto proyectoFinalizado = proyectoRepository.findById(PROYECTO_FINALIZADO_ID).orElseThrow();

        // Asignar empleados a múltiples proyectos (relaciones ManyToMany)

        // Senior Developer - trabaja en todos los proyectos (el más experimentado)
        senior.getProyectos().add(proyectoActivo);
        senior.getProyectos().add(proyectoSinFin);
        senior.getProyectos().add(proyectoFinalizado);

        // Desarrollador - trabaja en proyecto activo y sin fin
        desarrollador.getProyectos().add(proyectoActivo);
        desarrollador.getProyectos().add(proyectoSinFin);

        // Junior Developer - solo en proyecto activo (aprendiendo)
        junior.getProyectos().add(proyectoActivo);

        // Marketing Senior - en proyecto activo y proyecto finalizado
        marketingSenior.getProyectos().add(proyectoActivo);
        marketingSenior.getProyectos().add(proyectoFinalizado);

        // Marketing Regular - solo en proyecto sin fin
        marketing.getProyectos().add(proyectoSinFin);

        // RRHH Senior - en proyecto activo (para temas de personal)
        rrhhSenior.getProyectos().add(proyectoActivo);

        // Guardar las relaciones
        empleadoRepository.save(senior);
        empleadoRepository.save(desarrollador);
        empleadoRepository.save(junior);
        empleadoRepository.save(marketingSenior);
        empleadoRepository.save(marketing);
        empleadoRepository.save(rrhhSenior);
    }

    /**
     * Acceso rápido a TestDataFactory
     */
    protected TestDataFactory getTestData() {
        return new TestDataFactory();
    }
}
