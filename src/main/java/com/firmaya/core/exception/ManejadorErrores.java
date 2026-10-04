package com.firmaya.core.exception;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte las excepciones en respuestas HTTP con un formato común.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse("Hay campos con errores");
        for (FieldError campo : ex.getBindingResult().getFieldErrors()) {
            // Se conserva el primer error de cada campo
            if (!error.getErrores().containsKey(campo.getField())) {
                error.getErrores().put(campo.getField(), campo.getDefaultMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Parámetros de la URL con formato inválido (por ejemplo una fecha que no es DD/MM/AAAA)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> formatoInvalido(MethodArgumentTypeMismatchException ex) {
        ErrorResponse error = new ErrorResponse("Hay campos con errores");
        if (LocalDate.class.equals(ex.getRequiredType())) {
            error.getErrores().put(ex.getName(), "La fecha debe tener el formato DD/MM/AAAA");
        } else {
            error.getErrores().put(ex.getName(), "El valor ingresado no tiene un formato válido");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(MissingServletRequestParameterException ex) {
        ErrorResponse error = new ErrorResponse("Hay campos con errores");
        error.getErrores().put(ex.getParameterName(), "El campo " + ex.getParameterName() + " es obligatorio");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        if (ex.getCampo() != null) {
            error.getErrores().put(ex.getCampo(), ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConfirmacionRequeridaException.class)
    public ResponseEntity<ErrorResponse> confirmacionRequerida(ConfirmacionRequeridaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ErrorInternoException.class)
    public ResponseEntity<ErrorResponse> errorInterno(ErrorInternoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(NoAutenticadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }
}
