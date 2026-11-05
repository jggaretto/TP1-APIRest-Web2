package com.desarrolloweb.tp1.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FavoritoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarFavoritoNoEncontrado(FavoritoNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ListaNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarListaNoEncontrada(ListaNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ListaNoVaciaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarListaNoVacia(ListaNoVaciaException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarEmailYaRegistrado(EmailYaRegistradoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoAutenticadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoAutenticado(UsuarioNoAutenticadoException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * 403 (y no 404) cuando el recurso existe pero pertenece a otro usuario:
     * no confundimos "no existe" con "no es tuyo".
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAccesoDenegado(AccesoDenegadoException ex) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new HashMap<>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            detalles.put(error.getField(), error.getDefaultMessage());
        }
        var body = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Uno o más campos no son válidos",
                detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> manejarRequestInvalida(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo o los parámetros de la request no son válidos");
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponseDTO> manejarFallaExterna(ExternalServiceException ex) {
        return respuesta(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(HttpStatus status, String message) {
        var body = new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}
