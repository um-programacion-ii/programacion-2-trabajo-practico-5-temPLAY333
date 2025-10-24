package um.prog2.TP5.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.service.EmpleadoService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de empleados.
 * Proporciona endpoints para operaciones CRUD y consultas específicas sobre empleados.
 *
 * @author TP5
 * @version 1.0
 */
@RestController
@RequestMapping("/api/empleados")
@Validated
public class EmpleadoController {
    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    /**
     * Obtiene la lista completa de todos los empleados.
     *
     * @return Lista de todos los empleados registrados en el sistema
     * @apiNote GET /api/empleados
     */
    @GetMapping
    public List<Empleado> obtenerTodos() {
        return empleadoService.obtenerTodos();
    }

    /**
     * Busca un empleado específico por su ID.
     *
     * @param id Identificador único del empleado
     * @return Empleado encontrado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el empleado no existe
     * @apiNote GET /api/empleados/{id}
     */
    @GetMapping("/{id}")
    public Empleado obtenerPorId(@PathVariable Long id) {
        return empleadoService.buscarPorId(id);
    }

    /**
     * Crea un nuevo empleado en el sistema.
     *
     * @param empleado Objeto empleado con los datos a crear (validado)
     * @return Empleado creado con su ID asignado
     * @apiNote POST /api/empleados
     * @apiNote Status: 201 CREATED
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Empleado crear(@Valid @RequestBody Empleado empleado) {
        return empleadoService.validarEmpleado(empleado);
    }

    /**
     * Actualiza los datos de un empleado existente.
     *
     * @param id       Identificador del empleado a actualizar
     * @param empleado Objeto con los nuevos datos del empleado (validado)
     * @return Empleado actualizado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el empleado no existe
     * @apiNote PUT /api/empleados/{id}
     */
    @PutMapping("/{id}")
    public Empleado actualizar(@PathVariable Long id, @Valid @RequestBody Empleado empleado) {
        return empleadoService.actualizar(id, empleado);
    }

    /**
     * Elimina un empleado del sistema.
     *
     * @param id Identificador del empleado a eliminar
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el empleado no existe
     * @apiNote DELETE /api/empleados/{id}
     * @apiNote Status: 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        empleadoService.eliminar(id);
    }

    /**
     * Busca todos los empleados que pertenecen a un departamento específico.
     *
     * @param nombre Nombre del departamento
     * @return Lista de empleados del departamento
     * @apiNote GET /api/empleados/departamento/{nombre}
     */
    @GetMapping("/departamento/{nombre}")
    public List<Empleado> obtenerPorDepartamento(@PathVariable String nombre) {
        return empleadoService.buscarPorDepartamento(nombre);
    }

    /**
     * Busca un empleado por su dirección de correo electrónico.
     *
     * @param email Dirección de correo electrónico del empleado
     * @return Empleado encontrado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si no existe empleado con ese email
     * @apiNote GET /api/empleados/email/{email}
     */
    @GetMapping("/email/{email}")
    public Empleado obtenerPorEmail(@PathVariable String email) {
        return empleadoService.buscarPorEmail(email);
    }

    /**
     * Busca empleados cuyo salario sea mayor al monto especificado.
     *
     * @param salarioMinimo Salario mínimo de búsqueda
     * @return Lista de empleados con salario mayor al especificado
     * @apiNote GET /api/empleados/salario?salarioMinimo={monto}
     * @apiNote Ejemplo: GET /api/empleados/salario?salarioMinimo=50000
     */
    @GetMapping("/salario")
    public List<Empleado> obtenerPorSalarioMayorA(@RequestParam BigDecimal salarioMinimo, @RequestParam BigDecimal salarioMaximo) {
        return empleadoService.buscarPorRangoSalario(salarioMinimo, salarioMaximo);
    }
}
