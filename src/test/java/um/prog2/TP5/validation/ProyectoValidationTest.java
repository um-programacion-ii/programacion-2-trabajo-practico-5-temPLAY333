package um.prog2.TP5.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.testutil.TestDataFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de validación para la entidad Proyecto
 */
@SpringBootTest
@ActiveProfiles("test")
class ProyectoValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void proyectoValido_noDeberiaGenerarViolaciones() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void nombreVacio_deberiaGenerarViolacion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setNombre("");

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        // @NotBlank puede generar múltiples violaciones
        assertThat(violaciones)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(mensaje -> mensaje.contains("nombre") || mensaje.contains("obligatorio"));
    }

    @Test
    void nombreMuyCorto_deberiaGenerarViolacion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setNombre("AB"); // Solo 2 caracteres

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El nombre debe tener entre 3 y 100 caracteres");
    }

    @Test
    void nombreMuyLargo_deberiaGenerarViolacion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setNombre("A".repeat(101)); // 101 caracteres

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El nombre debe tener entre 3 y 100 caracteres");
    }

    @Test
    void descripcionMuyLarga_deberiaGenerarViolacion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setDescripcion("A".repeat(1001)); // 1001 caracteres

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("La descripción no puede exceder 1000 caracteres");
    }

    @Test
    void descripcionNula_deberiaSerValida() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setDescripcion(null);

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void fechaFinAnteriorAInicio_deberiaGenerarViolacion() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setFechaInicio(LocalDate.of(2024, 1, 15));
        proyecto.setFechaFin(LocalDate.of(2024, 1, 10)); // Anterior a inicio

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("La fecha de fin no puede ser anterior a la fecha de inicio");
    }

    @Test
    void fechaFinIgualAInicio_deberiaSerValida() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        proyecto.setFechaInicio(fecha);
        proyecto.setFechaFin(fecha); // Mismo día

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void fechaFinPosteriorAInicio_deberiaSerValida() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setFechaInicio(LocalDate.of(2024, 1, 10));
        proyecto.setFechaFin(LocalDate.of(2024, 1, 20)); // Posterior a inicio

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void fechasNulas_deberianSerValidas() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setFechaInicio(null);
        proyecto.setFechaFin(null);

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void soloFechaInicio_deberiaSerValida() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setFechaInicio(LocalDate.of(2024, 1, 15));
        proyecto.setFechaFin(null); // Sin fecha fin

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void soloFechaFin_deberiaSerValida() {
        // Given
        Proyecto proyecto = TestDataFactory.crearProyectoActivo("Proyecto Test");
        proyecto.setFechaInicio(null);
        proyecto.setFechaFin(LocalDate.of(2024, 1, 15));

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void variasViolaciones_deberiaGenerarTodasLasViolaciones() {
        // Given
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(""); // Violación: nombre vacío (puede generar @NotBlank + @Size)
        proyecto.setDescripcion("A".repeat(1001)); // Violación: descripción muy larga
        proyecto.setFechaInicio(LocalDate.of(2024, 1, 20));
        proyecto.setFechaFin(LocalDate.of(2024, 1, 10)); // Violación: fecha fin anterior a inicio

        // When
        Set<ConstraintViolation<Proyecto>> violaciones = validator.validate(proyecto);

        // Then
        // Bean Validation puede generar más violaciones debido a que @NotBlank y @Size se activan juntos
        assertThat(violaciones).hasSizeGreaterThanOrEqualTo(3);

        // Verificamos que contengan los conceptos clave en lugar de mensajes exactos
        String mensajesViolaciones = violaciones.stream()
                .map(ConstraintViolation::getMessage)
                .reduce("", (a, b) -> a + " " + b);

        assertThat(mensajesViolaciones).contains("nombre");
        assertThat(mensajesViolaciones).contains("descripción");
        assertThat(mensajesViolaciones).contains("fecha");
    }
}
