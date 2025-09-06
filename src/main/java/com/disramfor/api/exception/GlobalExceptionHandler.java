package com.disramfor.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manejador de excepciones global para toda la aplicación.
 * Captura excepciones específicas y las transforma en respuestas JSON estandarizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de recursos no encontrados (404 Not Found).
     * Ocurre típicamente cuando se busca una entidad por un ID que no existe.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        logger.warn("Recurso no encontrado en la solicitud a [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = createErrorResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de validación de argumentos de métodos (400 Bad Request).
     * Se activa cuando un DTO anotado con @Valid falla la validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Error de validación en la solicitud a [{}]: {}", request.getRequestURI(), errors);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación")
                .message("Los datos enviados no son válidos. Por favor, revise los campos.")
                .validationErrors(errors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de integridad de datos (409 Conflict).
     * Ocurre al violar constraints de la BD (ej. duplicados, llaves foráneas).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Error de integridad de datos. Posiblemente un valor duplicado o una referencia incorrecta.";
        String rootCauseMessage = ex.getMostSpecificCause().getMessage().toLowerCase();

        if (rootCauseMessage.contains("duplicate entry") || rootCauseMessage.contains("unique constraint")) {
            message = "El registro ya existe en el sistema.";
        } else if (rootCauseMessage.contains("foreign key constraint")) {
            message = "No se puede eliminar o actualizar porque tiene registros relacionados.";
        }

        logger.warn("Conflicto de integridad de datos en [{}]: {}", request.getRequestURI(), message);
        ErrorResponse error = createErrorResponse(HttpStatus.CONFLICT, "Conflicto de datos", message, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones personalizadas de la lógica de negocio (400 Bad Request).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.warn("Error de negocio en la solicitud a [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = createErrorResponse(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja el caso en que el cuerpo de la solicitud (JSON) está mal formado (400 Bad Request).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("JSON mal formado en la solicitud a [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = createErrorResponse(HttpStatus.BAD_REQUEST, "JSON mal formado", "El cuerpo de la solicitud no es un JSON válido.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja el error cuando un parámetro de la URL tiene un tipo incorrecto (400 Bad Request).
     * Ej: Se esperaba un número y se recibió texto.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("El parámetro '%s' debe ser de tipo '%s'.", ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());
        logger.warn("Tipo de argumento incorrecto en [{}]: {}", request.getRequestURI(), message);
        ErrorResponse error = createErrorResponse(HttpStatus.BAD_REQUEST, "Tipo de parámetro incorrecto", message, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja el error cuando se usa un método HTTP incorrecto para un endpoint (405 Method Not Allowed).
     * Ej: Usar POST en un endpoint que solo permite GET.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Método '%s' no soportado. Métodos permitidos: %s", ex.getMethod(), ex.getSupportedHttpMethods());
        logger.warn("Método HTTP no soportado en [{}]: {}", request.getRequestURI(), message);
        ErrorResponse error = createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", message, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Maneja errores de acceso denegado por Spring Security (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acceso denegado en la solicitud a [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = createErrorResponse(HttpStatus.FORBIDDEN, "Acceso denegado", "No tiene los permisos necesarios para acceder a este recurso spring roles.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Manejador genérico para cualquier otra excepción no controlada (500 Internal Server Error).
     * Este es el último recurso para evitar que la aplicación falle sin una respuesta controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // Logueamos el error con nivel ERROR y el stack trace completo.
        logger.error("Error no esperado en la solicitud a [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "Ha ocurrido un error inesperado. Contacte al administrador.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Método de utilidad para crear un objeto ErrorResponse estándar.
     */
    private ErrorResponse createErrorResponse(HttpStatus status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
