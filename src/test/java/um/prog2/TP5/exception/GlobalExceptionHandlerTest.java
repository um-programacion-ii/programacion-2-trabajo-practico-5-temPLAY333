package um.prog2.TP5.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import um.prog2.TP5.entity.Departamento;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleEmpleadoNoEncontrado_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "Empleado con ID 1 no encontrado";
        EmpleadoNoEncontradoException ex = new EmpleadoNoEncontradoException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleEmpleadoNoEncontrado(ex);

        // Then
        assertEquals("EMPLEADO_NO_ENCONTRADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleDepartamentoNoEncontrado_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "Departamento con ID 1 no encontrado";
        DepartamentoNoEncontradoException ex = new DepartamentoNoEncontradoException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleDepartamentoNoEncontrado(ex);

        // Then
        assertEquals("DEPARTAMENTO_NO_ENCONTRADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleProyectoNoEncontrado_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "Proyecto con ID 1 no encontrado";
        ProyectoNoEncontradoException ex = new ProyectoNoEncontradoException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleProyectoNoEncontrado(ex);

        // Then
        assertEquals("PROYECTO_NO_ENCONTRADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleResourceNotFound_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "Recurso no encontrado";
        ResourceNotFoundException ex = new ResourceNotFoundException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleResourceNotFound(ex);

        // Then
        assertEquals("RECURSO_NO_ENCONTRADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleHttpMediaTypeNotSupported_conContentType_deberiaRetornarErrorResponse() {
        // Given
        MediaType unsupportedType = MediaType.APPLICATION_XML;
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
            unsupportedType, Arrays.asList(MediaType.APPLICATION_JSON)
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMediaTypeNotSupported(ex);

        // Then
        assertEquals("TIPO_MEDIA_NO_SOPORTADO", response.getCodigo());
        assertTrue(response.getMensaje().contains("application/xml"));
    }

    @Test
    void handleHttpMediaTypeNotSupported_sinContentType_deberiaRetornarErrorResponse() {
        // Given
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
            "Content type not supported"
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMediaTypeNotSupported(ex);

        // Then
        assertEquals("TIPO_MEDIA_NO_SOPORTADO", response.getCodigo());
        assertTrue(response.getMensaje().contains("desconocido"));
    }

    @Test
    void handleHttpMediaTypeNotAcceptable_deberiaRetornarErrorResponse() {
        // Given
        HttpMediaTypeNotAcceptableException ex = new HttpMediaTypeNotAcceptableException(
            Arrays.asList(MediaType.APPLICATION_JSON)
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMediaTypeNotAcceptable(ex);

        // Then
        assertEquals("TIPO_MEDIA_NO_ACEPTABLE", response.getCodigo());
        assertEquals("Tipo de contenido no aceptable", response.getMensaje());
    }

    @Test
    void handleHttpMessageNotReadable_conJSON_deberiaRetornarErrorResponse() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "JSON parse error: malformed JSON"
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals("MENSAJE_NO_LEGIBLE", response.getCodigo());
        assertEquals("JSON malformado o inválido", response.getMensaje());
    }

    @Test
    void handleHttpMessageNotReadable_sinJSON_deberiaRetornarErrorResponse() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Could not read document"
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals("MENSAJE_NO_LEGIBLE", response.getCodigo());
        assertEquals("El cuerpo de la petición no se puede leer", response.getMensaje());
    }

    @Test
    void handleMissingServletRequestParameter_deberiaRetornarErrorResponse() {
        // Given
        String parameterName = "id";
        String parameterType = "Long";
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException(
            parameterName, parameterType
        );

        // When
        ErrorResponse response = globalExceptionHandler.handleMissingServletRequestParameter(ex);

        // Then
        assertEquals("PARAMETRO_FALTANTE", response.getCodigo());
        assertTrue(response.getMensaje().contains(parameterName));
    }

    @Test
    void handleEmailDuplicado_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "El email ya existe";
        EmailDuplicadoException ex = new EmailDuplicadoException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleEmailDuplicado(ex);

        // Then
        assertEquals("EMAIL_DUPLICADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleDepartamentoDuplicado_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "El departamento ya existe";
        DepartamentoDuplicadoException ex = new DepartamentoDuplicadoException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleDepartamentoDuplicado(ex);

        // Then
        assertEquals("DEPARTAMENTO_DUPLICADO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleValidationErrors_deberiaRetornarErrorResponseConErroresConcatenados() {
        // Given
        Departamento departamento = new Departamento();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(departamento, "departamento");

        bindingResult.addError(new FieldError("departamento", "nombre", "El nombre es obligatorio"));
        bindingResult.addError(new FieldError("departamento", "descripcion", "La descripción es muy larga"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ErrorResponse response = globalExceptionHandler.handleValidationErrors(ex);

        // Then
        assertEquals("ERROR_VALIDACION", response.getCodigo());
        assertTrue(response.getMensaje().contains("nombre: El nombre es obligatorio"));
        assertTrue(response.getMensaje().contains("descripcion: La descripción es muy larga"));
    }

    @Test
    void handleIllegalArgument_deberiaRetornarErrorResponse() {
        // Given
        String mensaje = "Argumento inválido proporcionado";
        IllegalArgumentException ex = new IllegalArgumentException(mensaje);

        // When
        ErrorResponse response = globalExceptionHandler.handleIllegalArgument(ex);

        // Then
        assertEquals("ARGUMENTO_INVALIDO", response.getCodigo());
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void handleGenericException_deberiaRetornarErrorResponseGenerico() {
        // Given
        RuntimeException ex = new RuntimeException("Error inesperado interno");

        // When
        ErrorResponse response = globalExceptionHandler.handleGenericException(ex);

        // Then
        assertEquals("ERROR_INTERNO", response.getCodigo());
        assertEquals("Ha ocurrido un error inesperado", response.getMensaje());
        // Verificar que no se expone el mensaje interno
        assertFalse(response.getMensaje().contains("Error inesperado interno"));
    }

    @Test
    void handleHttpMessageNotReadable_conMensajeNull_deberiaRetornarMensajeGenerico() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("", (Throwable) null);

        // When
        ErrorResponse response = globalExceptionHandler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals("MENSAJE_NO_LEGIBLE", response.getCodigo());
        assertEquals("El cuerpo de la petición no se puede leer", response.getMensaje());
    }
}
