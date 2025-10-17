package um.prog2.TP5.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AsignacionEmpleadosRequest {

    @NotEmpty(message = "La lista de empleados no puede estar vacía")
    private List<Long> empleadosIds;

    public AsignacionEmpleadosRequest() {
    }

    public AsignacionEmpleadosRequest(List<Long> empleadosIds) {
        this.empleadosIds = empleadosIds;
    }

    public List<Long> getEmpleadosIds() {
        return empleadosIds;
    }

    public void setEmpleadosIds(List<Long> empleadosIds) {
        this.empleadosIds = empleadosIds;
    }
}
