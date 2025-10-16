package um.prog2.TP5.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.testutil.TestDataFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para ProyectoRepository usando @DataJpaTest para mayor velocidad
 */
@DataJpaTest
@ActiveProfiles("test")
class ProyectoRepositoryTest {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findActiveProjects_deberiaRetornarProyectosActivos() {
        // Given - Crear datos de prueba
        Proyecto proyectoActivo1 = TestDataFactory.crearProyectoActivo("Proyecto Activo 1");
        Proyecto proyectoActivo2 = TestDataFactory.crearProyectoSinFechaFin("Proyecto Sin Fin");
        Proyecto proyectoFinalizado = TestDataFactory.crearProyectoFinalizado("Proyecto Finalizado");

        entityManager.persist(proyectoActivo1);
        entityManager.persist(proyectoActivo2);
        entityManager.persist(proyectoFinalizado);
        entityManager.flush();

        // When - Ejecutar consulta
        List<Proyecto> proyectosActivos = proyectoRepository.findActiveProjects();

        // Then - Verificar resultados
        assertThat(proyectosActivos)
                .hasSize(2)
                .extracting(Proyecto::getNombre)
                .containsExactlyInAnyOrder("Proyecto Activo 1", "Proyecto Sin Fin");
    }

    @Test
    void findActiveProjects_sinProyectos_deberiaRetornarListaVacia() {
        // When
        List<Proyecto> proyectosActivos = proyectoRepository.findActiveProjects();

        // Then
        assertThat(proyectosActivos).isEmpty();
    }

    @Test
    void findActiveProjects_soloProyectosFinalizados_deberiaRetornarListaVacia() {
        // Given
        Proyecto proyectoFinalizado1 = TestDataFactory.crearProyectoFinalizado("Proyecto Viejo 1");
        Proyecto proyectoFinalizado2 = TestDataFactory.crearProyectoFinalizado("Proyecto Viejo 2");

        entityManager.persist(proyectoFinalizado1);
        entityManager.persist(proyectoFinalizado2);
        entityManager.flush();

        // When
        List<Proyecto> proyectosActivos = proyectoRepository.findActiveProjects();

        // Then
        assertThat(proyectosActivos).isEmpty();
    }

    @Test
    void findById_proyectoExistente_deberiaRetornarProyecto() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Mi Proyecto");
        entityManager.persistAndFlush(proyecto);

        // When
        Proyecto encontrado = proyectoRepository.findById(proyecto.getId()).orElse(null);

        // Then
        assertThat(encontrado)
                .isNotNull()
                .extracting(Proyecto::getNombre)
                .isEqualTo("Mi Proyecto");
    }
}
