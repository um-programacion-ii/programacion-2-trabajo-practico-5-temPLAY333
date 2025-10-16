package um.prog2.TP5.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.testutil.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para DepartamentoRepository usando @DataJpaTest para mayor velocidad
 */
@DataJpaTest
@ActiveProfiles("test")
class DepartamentoRepositoryTest {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByNombre_departamentoExistente_deberiaRetornarDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamento("Desarrollo");
        entityManager.persistAndFlush(departamento);

        // When
        Optional<Departamento> encontrado = departamentoRepository.findByNombre("Desarrollo");

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .satisfies(d -> {
                    assertThat(d.getNombre()).isEqualTo("Desarrollo");
                    assertThat(d.getDescripcion()).isEqualTo("Descripción del departamento Desarrollo");
                });
    }

    @Test
    void findByNombre_departamentoInexistente_deberiaRetornarEmpty() {
        // When
        Optional<Departamento> encontrado = departamentoRepository.findByNombre("NoExiste");

        // Then
        assertThat(encontrado).isEmpty();
    }

    @Test
    void findByNombre_conVariosDeptos_deberiaRetornarElCorrecto() {
        // Given
        Departamento desarrollo = TestDataFactory.crearDepartamentoDesarrollo();
        Departamento marketing = TestDataFactory.crearDepartamentoMarketing();
        Departamento rrhh = TestDataFactory.crearDepartamentoRecursosHumanos();

        entityManager.persist(desarrollo);
        entityManager.persist(marketing);
        entityManager.persist(rrhh);
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
    void save_departamentoValido_deberiaGuardarCorrectamente() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamento("Finanzas");

        // When
        Departamento guardado = departamentoRepository.save(departamento);

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
    void findById_departamentoExistente_deberiaRetornarDepartamento() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamentoVentas();
        entityManager.persistAndFlush(departamento);

        // When
        Optional<Departamento> encontrado = departamentoRepository.findById(departamento.getId());

        // Then
        assertThat(encontrado)
                .isPresent()
                .get()
                .extracting(Departamento::getNombre)
                .isEqualTo("Ventas");
    }

    @Test
    void existsById_departamentoExistente_deberiaRetornarTrue() {
        // Given
        Departamento departamento = TestDataFactory.crearDepartamento("IT");
        entityManager.persistAndFlush(departamento);

        // When
        boolean existe = departamentoRepository.existsById(departamento.getId());

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    void existsById_departamentoInexistente_deberiaRetornarFalse() {
        // When
        boolean existe = departamentoRepository.existsById(999L);

        // Then
        assertThat(existe).isFalse();
    }
}
