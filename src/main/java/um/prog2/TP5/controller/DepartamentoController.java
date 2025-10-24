package um.prog2.TP5.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.service.DepartamentoService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de departamentos.
 * Proporciona endpoints para operaciones CRUD y consultas específicas sobre departamentos.
 *
 * @author TP5
 * @version 1.0
 */
@RestController
@RequestMapping("/api/departamentos")
@Validated
public class DepartamentoController {
    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    /**
     * Obtiene la lista completa de todos los departamentos.
     *
     * @return Lista de todos los departamentos registrados en el sistema
     * @apiNote GET /api/departamentos
     */
    @GetMapping
    public List<Departamento> obtenerTodos() {
        return departamentoService.obtenerTodos();
    }

    /**
     * Busca un departamento específico por su ID.
     *
     * @param id Identificador único del departamento
     * @return Departamento encontrado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el departamento no existe
     * @apiNote GET /api/departamentos/{id}
     */
    @GetMapping("/{id}")
    public Departamento obtenerPorId(@PathVariable Long id) {
        return departamentoService.buscarPorId(id);
    }

    /**
     * Crea un nuevo departamento en el sistema.
     *
     * @param departamento Objeto departamento con los datos a crear (validado)
     * @return Departamento creado con su ID asignado
     * @apiNote POST /api/departamentos
     * @apiNote Status: 201 CREATED
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Departamento crear(@Valid @RequestBody Departamento departamento) {
        return departamentoService.guardar(departamento);
    }

    /**
     * Actualiza los datos de un departamento existente.
     *
     * @param id           Identificador del departamento a actualizar
     * @param departamento Objeto con los nuevos datos del departamento (validado)
     * @return Departamento actualizado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el departamento no existe
     * @apiNote PUT /api/departamentos/{id}
     */
    @PutMapping("/{id}")
    public Departamento actualizar(@PathVariable Long id, @Valid @RequestBody Departamento departamento) {
        return departamentoService.actualizar(id, departamento);
    }

    /**
     * Elimina un departamento del sistema.
     *
     * @param id Identificador del departamento a eliminar
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si el departamento no existe
     * @apiNote DELETE /api/departamentos/{id}
     * @apiNote Status: 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        departamentoService.eliminar(id);
    }

    /**
     * Busca un departamento por su nombre exacto.
     *
     * @param nombre Nombre del departamento
     * @return Departamento encontrado
     * @throws um.prog2.TP5.exception.ResourceNotFoundException si no existe departamento con ese nombre
     * @apiNote GET /api/departamentos/nombre/{nombre}
     */
    @GetMapping("/nombre/{nombre}")
    public Departamento obtenerPorNombre(@PathVariable String nombre) {
        return departamentoService.buscarPorNombre(nombre);
    }
}