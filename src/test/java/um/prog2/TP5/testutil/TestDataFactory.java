package um.prog2.TP5.testutil;

import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Factory para crear datos de prueba de manera consistente
 */
public class TestDataFactory {

    // Métodos para Proyecto (existentes)
    public static Proyecto crearProyecto(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(nombre);
        proyecto.setDescripcion("Descripción del proyecto " + nombre);
        proyecto.setFechaInicio(fechaInicio);
        proyecto.setFechaFin(fechaFin);
        return proyecto;
    }

    public static Proyecto crearProyectoActivo(String nombre) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(nombre);
        proyecto.setDescripcion("Descripción del proyecto " + nombre);
        proyecto.setFechaInicio(LocalDate.now().minusDays(30));
        proyecto.setFechaFin(LocalDate.now().plusDays(30)); // Activo (fin en el futuro)
        return proyecto;
    }

    public static Proyecto crearProyectoFinalizado(String nombre) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(nombre);
        proyecto.setDescripcion("Descripción del proyecto " + nombre);
        proyecto.setFechaInicio(LocalDate.now().minusDays(60));
        proyecto.setFechaFin(LocalDate.now().minusDays(10)); // Finalizado
        return proyecto;
    }

    public static Proyecto crearProyectoSinFechaFin(String nombre) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(nombre);
        proyecto.setDescripcion("Descripción del proyecto " + nombre);
        proyecto.setFechaInicio(LocalDate.now().minusDays(15));
        proyecto.setFechaFin(null); // Sin fecha fin (activo)
        return proyecto;
    }

    // Métodos para Departamento (nuevos)
    public static Departamento crearDepartamento(String nombre) {
        Departamento departamento = new Departamento();
        departamento.setNombre(nombre);
        departamento.setDescripcion("Descripción del departamento " + nombre);
        return departamento;
    }

    public static Departamento crearDepartamentoDesarrollo() {
        return crearDepartamento("Desarrollo");
    }

    public static Departamento crearDepartamentoMarketing() {
        return crearDepartamento("Marketing");
    }

    public static Departamento crearDepartamentoRecursosHumanos() {
        return crearDepartamento("Recursos Humanos");
    }

    public static Departamento crearDepartamentoVentas() {
        return crearDepartamento("Ventas");
    }

    // Métodos para Empleado (nuevos)
    public static Empleado crearEmpleado(String nombre, String apellido, String email,
                                        BigDecimal salario, Departamento departamento) {
        Empleado empleado = new Empleado();
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setEmail(email);
        empleado.setSalario(salario);
        empleado.setFechaContratacion(LocalDate.now().minusMonths(6)); // Contratado hace 6 meses - VÁLIDA
        empleado.setDepartamento(departamento);
        return empleado;
    }

    public static Empleado crearEmpleadoDesarrollador(Departamento departamento) {
        return crearEmpleado("Juan", "Pérez", "juan.perez@empresa.com",
                new BigDecimal("75000.00"), departamento);
    }

    public static Empleado crearEmpleadoSenior(Departamento departamento) {
        Empleado empleado = crearEmpleado("María", "García", "maria.garcia@empresa.com",
                new BigDecimal("95000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusYears(3)); // Senior con 3 años - VÁLIDA
        return empleado;
    }

    public static Empleado crearEmpleadoJunior(Departamento departamento) {
        Empleado empleado = crearEmpleado("Carlos", "López", "carlos.lopez@empresa.com",
                new BigDecimal("45000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusMonths(2)); // Junior recién contratado - VÁLIDA
        return empleado;
    }

    public static Empleado crearEmpleadoMarketing(Departamento departamento) {
        return crearEmpleado("Ana", "Martínez", "ana.martinez@empresa.com",
                new BigDecimal("60000.00"), departamento);
    }

    public static Empleado crearEmpleadoRRHH(Departamento departamento) {
        return crearEmpleado("Pedro", "Rodríguez", "pedro.rodriguez@empresa.com",
                new BigDecimal("65000.00"), departamento);
    }

    // Empleados adicionales para Marketing (para no tener departamentos con un solo empleado)
    public static Empleado crearEmpleadoMarketingSenior(Departamento departamento) {
        Empleado empleado = crearEmpleado("Laura", "Fernández", "laura.fernandez@empresa.com",
                new BigDecimal("70000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusYears(2)); // Marketing Senior con 2 años
        return empleado;
    }

    public static Empleado crearEmpleadoMarketingJunior(Departamento departamento) {
        Empleado empleado = crearEmpleado("Diego", "Torres", "diego.torres@empresa.com",
                new BigDecimal("50000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusMonths(4)); // Marketing Junior
        return empleado;
    }

    // Empleados adicionales para RRHH (para no tener departamentos con un solo empleado)
    public static Empleado crearEmpleadoRRHHSenior(Departamento departamento) {
        Empleado empleado = crearEmpleado("Carmen", "Jiménez", "carmen.jimenez@empresa.com",
                new BigDecimal("72000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusYears(4)); // RRHH Senior con 4 años
        return empleado;
    }

    public static Empleado crearEmpleadoRRHHJunior(Departamento departamento) {
        Empleado empleado = crearEmpleado("Roberto", "Silva", "roberto.silva@empresa.com",
                new BigDecimal("48000.00"), departamento);
        empleado.setFechaContratacion(LocalDate.now().minusMonths(3)); // RRHH Junior
        return empleado;
    }
}
