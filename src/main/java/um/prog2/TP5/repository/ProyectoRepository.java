package um.prog2.TP5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import um.prog2.TP5.entity.Empleado;
import um.prog2.TP5.entity.Proyecto;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    @Query("SELECT p FROM Proyecto p WHERE p.fechaFin IS NULL OR p.fechaFin > CURRENT_DATE")
    List<Proyecto> findActiveProjects();
}
