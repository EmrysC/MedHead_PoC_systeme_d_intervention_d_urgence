package MeadHead.Poc.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import MeadHead.Poc.exception.ErrorDetails;
import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;
import MeadHead.Poc.exception.exeption_list.ValidationManuelleException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:prod}")
    private String activeProfile;

    // --- VALIDATION CLASSIQUE (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> {
                    String key = (error.getCodes() != null && error.getCodes().length > 0)
                            ? error.getCodes()[0].substring(error.getCodes()[0].lastIndexOf('.') + 1)
                            : "erreur_logique";

                    if (key.contains("DTO")) {
                        key = "choix_localisation";
                    }
                    errors.put(key, error.getDefaultMessage());
                });

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Erreur de validation des arguments",
                errors,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // --- VALIDATION MANUELLE (Celle qui posait problème) ---
    @ExceptionHandler(ValidationManuelleException.class)
    public ResponseEntity<Object> handleValidationManuelleException(
            ValidationManuelleException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        // On récupère le result d'abord
        org.springframework.validation.BindingResult result = ex.getBindingResult();

        if (result != null) {
            // Erreurs sur les champs
            result.getFieldErrors().forEach(error
                    -> errors.put(error.getField(), error.getDefaultMessage()));

            // Erreurs globales
            result.getGlobalErrors().forEach(error -> {
                String key = (error.getCodes() != null && error.getCodes().length > 0)
                        ? error.getCodes()[0].substring(error.getCodes()[0].lastIndexOf('.') + 1)
                        : "choix_localisation";

                if (key.contains("DTO")) {
                    key = "choix_localisation";
                }
                errors.put(key, error.getDefaultMessage());
            });
        } else {
            // Si pas de BindingResult (cas de ton test), on utilise le message simple
            errors.put("detail", ex.getMessage());
        }

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Erreur de validation des arguments",
                errors,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // --- GESTION DES RESSOURCES ET ACCÈS ---
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        Map<String, String> singleErrorMap = Map.of("detail", "L'endpoint demandé n'existe pas.");
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Ressource non trouvée", singleErrorMap, request.getDescription(false)), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String errorKey = (ex.getCause() != null) ? ex.getCause().getClass().getSimpleName() : ex.getClass().getSimpleName();
        Map<String, String> errorsMap = Map.of(errorKey, ex.getLocalizedMessage());
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Échec d'authentification", errorsMap, request.getDescription(false)), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, String> singleErrorMap = Map.of("detail", ex.getMessage() != null ? ex.getMessage() : "Accès refusé.");
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Accès Refusé", singleErrorMap, request.getDescription(false)), HttpStatus.FORBIDDEN);
    }

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

    // --- ERREURS DE FORMAT ET PARAMÈTRES ---
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, WebRequest request) {
        String typeAttendu = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "nombre";
        Map<String, String> errors = Map.of(ex.getName(), String.format("Le champ '%s' doit être un %s.", ex.getName(), typeAttendu));
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Erreur de format de paramètre", errors, request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(org.springframework.web.bind.MissingServletRequestParameterException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Paramètre manquant", Map.of(ex.getParameterName(), "Obligatoire"), request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Format JSON invalide", Map.of("format", "Donnée invalide."), request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    // --- EXCEPTIONS GÉNÉRIQUES ET TECHNIQUES ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorDetails(LocalDateTime.now(), "Erreur interne", Map.of("detail", "Erreur inconnue."), request.getDescription(false)), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean isDev() {
        return "dev".equals(activeProfile);
    }
}
