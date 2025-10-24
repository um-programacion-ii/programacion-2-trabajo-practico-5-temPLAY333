package um.prog2.TP5.service;

import um.prog2.TP5.entity.Empleado;

import java.math.BigDecimal;
import java.util.List;

// Interface del servicio
public interface EmpleadoService {
    Empleado validarEmpleado(Empleado empleado);
    Empleado buscarPorEmail(String email);
    Empleado buscarPorId(Long id);
    List<Empleado> buscarPorDepartamento(String nombreDepartamento);
    List<Empleado> buscarPorRangoSalario(BigDecimal salarioMin, BigDecimal salarioMax);
    BigDecimal obtenerSalarioPromedioPorDepartamento(Long departamentoId);
    List<Empleado> obtenerTodos();
    Empleado actualizar(Long id, Empleado empleado);
    void eliminar(Long id);
}
