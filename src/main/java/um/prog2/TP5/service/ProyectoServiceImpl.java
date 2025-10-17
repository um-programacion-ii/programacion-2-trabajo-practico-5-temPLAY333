package um.prog2.TP5.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;
import um.prog2.TP5.exception.EmpleadoNoEncontradoException;
import um.prog2.TP5.exception.ProyectoNoEncontradoException;
import um.prog2.TP5.repository.EmpleadoRepository;
import um.prog2.TP5.repository.ProyectoRepository;

import java.util.List;

@Service
@Transactional
public class ProyectoServiceImpl implements ProyectoService {
    private final ProyectoRepository proyectoRepository;
    private final EmpleadoRepository empleadoRepository;

    public ProyectoServiceImpl(ProyectoRepository proyectoRepository, EmpleadoRepository empleadoRepository) {
        this.proyectoRepository = proyectoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    @Override
    public Proyecto buscarPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("Proyecto no encontrado con ID: " + id));
    }

    @Override
    public List<Proyecto> obtenerTodos() {
        return proyectoRepository.findAll();
    }

    @Override
    public List<Proyecto> obtenerProyectosActivos() {
        return proyectoRepository.findActiveProjects();
    }

    @Override
    public Proyecto actualizar(Long id, Proyecto proyecto) {
        if (!proyectoRepository.existsById(id)) {
            throw new ProyectoNoEncontradoException("Proyecto no encontrado con ID: " + id);
        }
        proyecto.setId(id);
        return proyectoRepository.save(proyecto);
    }

    @Override
    public Proyecto asignarEmpleados(Proyecto proyecto, List<Long> empleadosIds) {
        // Limpiar empleados actuales del proyecto
        proyecto.getEmpleados().clear();

        // Buscar y asignar los nuevos empleados
        for (Long empleadoId : empleadosIds) {
            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new EmpleadoNoEncontradoException("Empleado no encontrado con ID: " + empleadoId));

            // Agregar el proyecto a los proyectos del empleado
            empleado.getProyectos().add(proyecto);
            // Agregar el empleado a los empleados del proyecto
            proyecto.getEmpleados().add(empleado);
        }

        return proyectoRepository.save(proyecto);
    }

    @Override
    public void eliminar(Long id) {
        if (!proyectoRepository.existsById(id)) {
            throw new ProyectoNoEncontradoException("Proyecto no encontrado con ID: " + id);
        }
        proyectoRepository.deleteById(id);
    }
}
