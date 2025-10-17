package um.prog2.TP5.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmpleadoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEmpleadoNoEncontrado(EmpleadoNoEncontradoException ex) {
        return new ErrorResponse("EMPLEADO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(DepartamentoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDepartamentoNoEncontrado(DepartamentoNoEncontradoException ex) {
        return new ErrorResponse("DEPARTAMENTO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ProyectoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleProyectoNoEncontrado(ProyectoNoEncontradoException ex) {
        return new ErrorResponse("PROYECTO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse("RECURSO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleHttpMediaTypeNotSupported(org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        String contentType = ex.getContentType() != null ? ex.getContentType().toString() : "desconocido";
        return new ErrorResponse("TIPO_MEDIA_NO_SOPORTADO", "Tipo de contenido no soportado: " + contentType);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleHttpMediaTypeNotAcceptable(org.springframework.web.HttpMediaTypeNotAcceptableException ex) {
        return new ErrorResponse("TIPO_MEDIA_NO_ACEPTABLE", "Tipo de contenido no aceptable");
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String mensaje = ex.getMessage() != null && ex.getMessage().contains("JSON")
            ? "JSON malformado o inválido"
            : "El cuerpo de la petición no se puede leer";
        return new ErrorResponse("MENSAJE_NO_LEGIBLE", mensaje);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        return new ErrorResponse("PARAMETRO_FALTANTE", "Parámetro requerido faltante: " + ex.getParameterName());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailDuplicado(EmailDuplicadoException ex) {
        return new ErrorResponse("EMAIL_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(DepartamentoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDepartamentoDuplicado(DepartamentoDuplicadoException ex) {
        return new ErrorResponse("DEPARTAMENTO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        String errores = bindingResult.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ErrorResponse("ERROR_VALIDACION", "Errores de validación: " + errores);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse("ARGUMENTO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        // Log la excepción para debugging pero no exponer detalles internos
        return new ErrorResponse("ERROR_INTERNO", "Ha ocurrido un error inesperado");
    }
}
