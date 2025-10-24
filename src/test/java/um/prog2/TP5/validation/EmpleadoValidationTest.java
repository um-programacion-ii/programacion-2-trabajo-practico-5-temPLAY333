package um.prog2.TP5.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.testutil.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de validación para la entidad Empleado
 */
@SpringBootTest
@ActiveProfiles("test")
class EmpleadoValidationTest {

    @Autowired
    private Validator validator;

    private Departamento departamentoValido;

    @BeforeEach
    void setUp() {
        departamentoValido = TestDataFactory.crearDepartamentoDesarrollo();
    }

    @Test
    void empleadoValido_noDeberiaGenerarViolaciones() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones).isEmpty();
    }

    @Test
    void nombreVacio_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setNombre("");

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        // Un nombre vacío puede generar tanto @NotBlank como @Size violaciones
        assertThat(violaciones)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(mensaje -> mensaje.contains("nombre") && (mensaje.contains("obligatorio") || mensaje.contains("entre")));
    }

    @Test
    void nombreMuyCorto_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setNombre("A");

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El nombre debe tener entre 2 y 50 caracteres");
    }

    @Test
    void nombreMuyLargo_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setNombre("A".repeat(51)); // 51 caracteres

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El nombre debe tener entre 2 y 50 caracteres");
    }

    @Test
    void emailInvalido_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setEmail("email-invalido");

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El formato del email es inválido");
    }

    @Test
    void emailVacio_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setEmail("");

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        // @NotBlank + @Email pueden generar diferentes números de violaciones
        assertThat(violaciones)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(mensaje -> mensaje.contains("email") || mensaje.contains("obligatorio") || mensaje.contains("inválido"));
    }

    @Test
    void salarioNegativo_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setSalario(new BigDecimal("-1000"));

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El salario debe ser mayor a 0");
    }

    @Test
    void salarioCero_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setSalario(BigDecimal.ZERO);

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El salario debe ser mayor a 0");
    }

    @Test
    void salarioNulo_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setSalario(null);

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El salario es obligatorio");
    }

    @Test
    void fechaContratacionFutura_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setFechaContratacion(LocalDate.now().plusDays(1));

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("La fecha de contratación no puede ser futura");
    }

    @Test
    void fechaContratacionNula_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setFechaContratacion(null);

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("La fecha de contratación es obligatoria");
    }

    @Test
    void departamentoNulo_deberiaGenerarViolacion() {
        // Given
        Empleado empleado = TestDataFactory.crearEmpleadoDesarrollador(departamentoValido);
        empleado.setDepartamento(null);

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        assertThat(violaciones)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El departamento es obligatorio");
    }

    @Test
    void variasViolaciones_deberiaGenerarTodasLasViolaciones() {
        // Given
        Empleado empleado = new Empleado();
        empleado.setNombre(""); // Violación: nombre vacío
        empleado.setApellido("A"); // Violación: apellido muy corto
        empleado.setEmail("email-invalido"); // Violación: email inválido
        empleado.setSalario(new BigDecimal("-100")); // Violación: salario negativo
        empleado.setFechaContratacion(LocalDate.now().plusDays(10)); // Violación: fecha futura
        // departamento null - Violación: departamento obligatorio

        // When
        Set<ConstraintViolation<Empleado>> violaciones = validator.validate(empleado);

        // Then
        // Bean Validation puede generar más violaciones de las esperadas
        assertThat(violaciones).hasSizeGreaterThanOrEqualTo(6);

        // Verificamos que contengan los conceptos clave
        String mensajesViolaciones = violaciones.stream()
                .map(ConstraintViolation::getMessage)
                .reduce("", (a, b) -> a + " " + b);

        assertThat(mensajesViolaciones).contains("nombre");
        assertThat(mensajesViolaciones).contains("apellido");
        assertThat(mensajesViolaciones).contains("email");
        assertThat(mensajesViolaciones).contains("salario");
        assertThat(mensajesViolaciones).contains("fecha");
        assertThat(mensajesViolaciones).contains("departamento");
    }
}
