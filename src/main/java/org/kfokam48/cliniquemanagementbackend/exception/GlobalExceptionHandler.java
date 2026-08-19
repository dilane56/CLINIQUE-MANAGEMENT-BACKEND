package org.kfokam48.cliniquemanagementbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return body;
    }

    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRessourceNotFoundException(RessourceNotFoundException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleResourceAlreadyExistException(ResourceAlreadyExistException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationFailedException(AuthenticationFailedException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTokenException(InvalidTokenException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessTokenRequiredException.class)
    public ResponseEntity<Map<String, String>> handleAccessTokenRequiredException(AccessTokenRequiredException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(errorBody("Accès refusé : " + ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RendezVousNonTermineException.class)
    public ResponseEntity<Map<String, String>> handleRendezVousNonTermineException(RendezVousNonTermineException e) {
        return new ResponseEntity<>(errorBody(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
