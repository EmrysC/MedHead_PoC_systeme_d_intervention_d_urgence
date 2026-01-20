package MeadHead.Poc.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;
import MeadHead.Poc.exception.exeption_list.ValidationManuelleException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // LOGIQUE COMMUNE D'EXTRACTION 
    private Map<String, String> extractBindingErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        if (result == null) {
            return errors;
        }

        result.getFieldErrors().forEach(error
                -> errors.put(error.getField(), error.getDefaultMessage()));

        result.getGlobalErrors().forEach(error -> {
            String[] codes = error.getCodes();
            String key = (codes != null && codes.length > 0)
                    ? codes[0].substring(codes[0].lastIndexOf('.') + 1)
                    : "choix_localisation";

            if (key.toLowerCase().contains("dto")) {
                key = "choix_localisation";
            }
            errors.put(key, error.getDefaultMessage());
        });
        return errors;
    }

    // --- VALIDATION CLASSIQUE (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        return buildBadRequestResponse("Erreur de validation des arguments", extractBindingErrors(ex.getBindingResult()), request);
    }

    // --- VALIDATION MANUELLE ---
    @ExceptionHandler(ValidationManuelleException.class)
    public ResponseEntity<Object> handleValidationManuelleException(ValidationManuelleException ex, WebRequest request) {
        Map<String, String> errors = extractBindingErrors(ex.getBindingResult());
        if (errors.isEmpty()) {
            errors.put("detail", ex.getMessage() != null ? ex.getMessage() : "Erreur de validation");
        }
        return buildBadRequestResponse("Erreur de validation manuelle", errors, request);
    }

    // --- GESTION DES ACCÈS (Sécurisé contre les valeurs nulles) ---
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        Throwable cause = ex.getCause();
        String errorKey = (cause != null) ? cause.getClass().getSimpleName() : ex.getClass().getSimpleName();

        Map<String, String> errorsMap = new HashMap<>();
        errorsMap.put(errorKey, ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "Échec d'authentification");

        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Authentification requise", errorsMap, request.getDescription(false)), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("detail", ex.getMessage() != null ? ex.getMessage() : "Accès refusé.");
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Accès Refusé", errors, request.getDescription(false)), HttpStatus.FORBIDDEN);
    }

    // --- EXCEPTIONS MÉTIER ---
    @ExceptionHandler(UniteSoinsNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUniteSoinsNotFoundException(UniteSoinsNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Ressource Introuvable", Map.of("id_unite_soins", ex.getMessage()), request.getDescription(false)), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LitIndisponibleException.class)
    public ResponseEntity<ErrorDetails> handleLitIndisponibleException(LitIndisponibleException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Conflit de Réservation", Map.of("disponibilite_lit", ex.getMessage()), request.getDescription(false)), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Conflit de données", ex.getErrors(), request.getDescription(false)), HttpStatus.CONFLICT);
    }

    // --- ERREURS TECHNIQUES ---
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Format JSON invalide", Map.of("format", "Donnée JSON malformée ou type incorrect."), request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("detail", "Une erreur interne est survenue.");
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Erreur système", errors, request.getDescription(false)), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- MÉTHODE UTILITAIRE POUR ÉVITER LA RÉPÉTITION ---
    private ResponseEntity<Object> buildBadRequestResponse(String message, Map<String, String> errors, WebRequest request) {
        ErrorDetails details = new ErrorDetails(LocalDateTime.now(), message, errors, request.getDescription(false));
        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);
    }
}
