package um.prog2.TP5.service;

import um.prog2.TP5.entity.Proyecto;

import java.util.List;

public interface ProyectoService {
    Proyecto guardar(Proyecto proyecto);
    Proyecto buscarPorId(Long id);
    List<Proyecto> obtenerTodos();
    List<Proyecto> obtenerProyectosActivos();
    Proyecto actualizar(Long id, Proyecto proyecto);
    Proyecto asignarEmpleados(Proyecto proyecto, List<Long> empleadosIds);
    void eliminar(Long id);
}
