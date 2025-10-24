package um.prog2.TP5.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import um.prog2.TP5.entity.Proyecto;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Proyecto> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // Inicialización si es necesaria
    }

    @Override
    public boolean isValid(Proyecto proyecto, ConstraintValidatorContext context) {
        if (proyecto == null) {
            return true; // Null values are handled by @NotNull
        }

        // Si no hay fechas, es válido
        if (proyecto.getFechaInicio() == null || proyecto.getFechaFin() == null) {
            return true;
        }

        // La fecha de fin debe ser posterior o igual a la fecha de inicio
        return !proyecto.getFechaFin().isBefore(proyecto.getFechaInicio());
    }
}
