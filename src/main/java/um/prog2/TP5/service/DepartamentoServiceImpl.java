package um.prog2.TP5.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import um.prog2.TP5.entity.Departamento;
import um.prog2.TP5.exception.DepartamentoDuplicadoException;
import um.prog2.TP5.exception.DepartamentoNoEncontradoException;
import um.prog2.TP5.repository.DepartamentoRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class DepartamentoServiceImpl implements DepartamentoService {
    private final DepartamentoRepository departamentoRepository;

    public DepartamentoServiceImpl(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    public Departamento guardar(Departamento departamento) {
        if (departamentoRepository.findByNombre(departamento.getNombre()).isPresent()) {
            throw new DepartamentoDuplicadoException("Ya existe un departamento con el nombre: " + departamento.getNombre());
        }
        return departamentoRepository.save(departamento);
    }

    @Override
    public Departamento buscarPorId(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new DepartamentoNoEncontradoException("Departamento no encontrado con ID: " + id));
    }

    @Override
    public Departamento buscarPorNombre(String nombre) {
        return departamentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new DepartamentoNoEncontradoException("Departamento no encontrado con nombre: " + nombre));
    }

    @Override
    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

    @Override
    public Departamento actualizar(Long id, Departamento departamento) {
        if (!departamentoRepository.existsById(id)) {
            throw new DepartamentoNoEncontradoException("Departamento no encontrado con ID: " + id);
        }
        departamento.setId(id);
        return departamentoRepository.save(departamento);
    }

    @Override
    public void eliminar(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new DepartamentoNoEncontradoException("Departamento no encontrado con ID: " + id);
        }
        departamentoRepository.deleteById(id);
    }
}
