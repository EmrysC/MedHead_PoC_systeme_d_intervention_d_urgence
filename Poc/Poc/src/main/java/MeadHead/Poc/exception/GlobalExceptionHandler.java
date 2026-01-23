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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.ExternalServiceFailureException;
import MeadHead.Poc.exception.exeption_list.GoogleMapsServiceFailureException;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.NoBedAvailableException;
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
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format("Le paramètre '%s' a une valeur invalide ('%s'). Type '%s' attendu.",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Format de paramètre incorrect", Map.of(ex.getName(), message), request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

// --- GESTION DES SERVICES EXTERNES (Google Maps, etc.) ---
    @ExceptionHandler({ExternalServiceFailureException.class, GoogleMapsServiceFailureException.class})
    public ResponseEntity<ErrorDetails> handleExternalServiceFailure(Exception ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        // On utilise le message de l'exception pour le détail technique
        errors.put("detail", ex.getMessage());

        ErrorDetails details = new ErrorDetails(
                LocalDateTime.now(),
                "Échec du service externe",
                errors,
                request.getDescription(false)
        );

        // Renvoie maintenant bien  502 
        return new ResponseEntity<>(details, HttpStatus.BAD_GATEWAY);
    }

    // --- MÉTHODE UTILITAIRE POUR ÉVITER LA RÉPÉTITION ---
    private ResponseEntity<Object> buildBadRequestResponse(String message, Map<String, String> errors, WebRequest request) {
        ErrorDetails details = new ErrorDetails(LocalDateTime.now(), message, errors, request.getDescription(false));
        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("ressource", "Le chemin demandé n'existe pas sur ce serveur.");

        return new ResponseEntity<>(
                new ErrorDetails(
                        LocalDateTime.now(),
                        "URL Invalide",
                        errors,
                        request.getDescription(false)
                ),
                HttpStatus.NOT_FOUND //  404 
        );
    }

    // --- GESTION DE L'ABSENCE DE LITS (404 ) ---
    @ExceptionHandler(NoBedAvailableException.class)
    public ResponseEntity<ErrorDetails> handleNoBedAvailableException(NoBedAvailableException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        String specId = request.getParameter("specialisationId");
        String detailMessage = ex.getMessage() + (specId != null ? " (id : " + specId + ")" : "");

        errors.put("specialisation", detailMessage);

        return new ResponseEntity<>(
                new ErrorDetails(
                        LocalDateTime.now(),
                        "Indisponibilité", // Le message global 
                        errors,
                        request.getDescription(false)
                ),
                HttpStatus.NOT_FOUND // 404 
        );
    }

}
