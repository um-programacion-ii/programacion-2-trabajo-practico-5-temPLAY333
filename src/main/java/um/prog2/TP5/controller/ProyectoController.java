package um.prog2.TP5.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import um.prog2.TP5.dto.AsignacionEmpleadosRequest;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.service.ProyectoService;

import java.util.List;

/**
 * Controlador REST para la gestión de proyectos.
 * Proporciona endpoints para operaciones CRUD, asignación de empleados y consultas específicas sobre proyectos.
 *
 * @author TP5
 * @version 1.0
 */
@RestController
@RequestMapping("/api/proyectos")
@Validated
public class ProyectoController {
    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    /**
     * Obtiene la lista completa de todos los proyectos.
     *
     * @return Lista de todos los proyectos registrados en el sistema
     * @apiNote GET /api/proyectos
     */
    @GetMapping
    public List<Proyecto> obtenerTodos() {
        return proyectoService.obtenerTodos();
    }

    /**
     * Busca un proyecto específico por su ID.
     *
     * @param id Identificador único del proyecto
     * @return Proyecto encontrado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto no existe
     * @apiNote GET /api/proyectos/{id}
     */
    @GetMapping("/{id}")
    public Proyecto obtenerPorId(@PathVariable Long id) {
        return proyectoService.buscarPorId(id);
    }

    /**
     * Crea un nuevo proyecto en el sistema.
     *
     * @param proyecto Objeto proyecto con los datos a crear (validado)
     * @return Proyecto creado con su ID asignado
     * @apiNote POST /api/proyectos
     * @apiNote Status: 201 CREATED
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proyecto crear(@Valid @RequestBody Proyecto proyecto) {
        return proyectoService.guardar(proyecto);
    }

    /**
     * Actualiza los datos de un proyecto existente.
     *
     * @param id       Identificador del proyecto a actualizar
     * @param proyecto Objeto con los nuevos datos del proyecto (validado)
     * @return Proyecto actualizado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto no existe
     * @apiNote PUT /api/proyectos/{id}
     */
    @PutMapping("/{id}")
    public Proyecto actualizar(@PathVariable Long id, @Valid @RequestBody Proyecto proyecto) {
        return proyectoService.actualizar(id, proyecto);
    }

    /**
     * Asigna una lista de empleados a un proyecto específico.
     * Los empleados se asignan mediante sus IDs.
     *
     * @param id      Identificador del proyecto
     * @param request Objeto con la lista de IDs de empleados a asignar
     * @return Proyecto con los empleados asignados
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto o algún empleado no existe
     * @apiNote POST /api/proyectos/{id}/asignar-empleados
     * @apiNote Body ejemplo: {"empleadosIds": [1, 2, 3]}
     */
    @PostMapping("/{id}/asignar-empleados")
    public Proyecto asignarEmpleados(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionEmpleadosRequest request) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        return proyectoService.asignarEmpleados(proyecto, request.getEmpleadosIds());
    }

    /**
     * Obtiene la lista de empleados asignados a un proyecto específico.
     *
     * @param id Identificador del proyecto
     * @return ResponseEntity con la lista de empleados del proyecto
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto no existe
     * @apiNote GET /api/proyectos/{id}/empleados
     */
    @GetMapping("/{id}/empleados")
    public ResponseEntity<?> obtenerEmpleadosDelProyecto(@PathVariable Long id) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        return ResponseEntity.ok(proyecto.getEmpleados());
    }

    /**
     * Actualiza la lista de empleados asignados a un proyecto (método alternativo).
     * Reemplaza completamente la lista de empleados del proyecto.
     *
     * @param id      Identificador del proyecto
     * @param request Objeto con la nueva lista de IDs de empleados
     * @return ResponseEntity con el proyecto actualizado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto o algún empleado no existe
     * @apiNote PUT /api/proyectos/{id}/empleados
     * @apiNote Body ejemplo: {"empleadosIds": [1, 2, 3]}
     */
    @PutMapping("/{id}/empleados")
    public ResponseEntity<Proyecto> asignarEmpleadosAlternativo(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionEmpleadosRequest request) {
        Proyecto proyecto = proyectoService.buscarPorId(id);
        Proyecto proyectoActualizado = proyectoService.asignarEmpleados(proyecto, request.getEmpleadosIds());

        return ResponseEntity.ok(proyectoActualizado);
    }


    /**
     * Elimina un proyecto del sistema.
     *
     * @param id Identificador del proyecto a eliminar
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el proyecto no existe
     * @apiNote DELETE /api/proyectos/{id}
     * @apiNote Status: 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
    }

    /**
     * Obtiene la lista de proyectos que están actualmente activos.
     *
     * @return Lista de proyectos activos
     * @apiNote GET /api/proyectos/activos
     */
    @GetMapping("/activos")
    public List<Proyecto> obtenerProyectosActivos() {
        return proyectoService.obtenerProyectosActivos();
    }
}
