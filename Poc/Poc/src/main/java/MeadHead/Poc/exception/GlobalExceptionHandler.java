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

import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des exceptions de Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        // On récupère les erreurs sur les champs classiques (@NotNull, @Size...)
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        // On récupère les erreurs globales ( @OneOfAddressOrGps)
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> {
                    // on a défini un nom de propriété dans le validator, on l'utilise, 
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

    // Gestion des ressources non trouvées
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFoundException(
            NoResourceFoundException ex, WebRequest request) {

        Map<String, String> singleErrorMap = Map.of(
                "detail", "L'endpoint demandé n'existe pas ou n'est pas mappé dans un contrôleur.");

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Ressource non trouvée",
                singleErrorMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // 404 Not found
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        String errorKey;
        String errorMessage;

        if (ex.getCause() != null) {
            errorKey = ex.getCause().getClass().getSimpleName();
            errorMessage = ex.getCause().getLocalizedMessage();
        } else {
            errorKey = ex.getClass().getSimpleName();
            errorMessage = ex.getLocalizedMessage();
        }

        // Création de la Map d'erreurs au format DTO
        Map<String, String> errorsMap = Map.of(
                errorKey, errorMessage);

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Échec",
                errorsMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    // GESTION DES CONFLITS (EmailAlreadyExistsException)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {

        Map<String, String> errorsMap = ex.getErrors();

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Conflit de données",
                errorsMap, // On passe la Map {"email": "Le message..."}
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409 colflict
    }

    // Gère les échecs d'autorisation (Accès refusé)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        Map<String, String> singleErrorMap = Map.of(
                "detail", ex.getMessage() != null ? ex.getMessage()
                : "Accès refusé. Vous n'avez pas les permissions nécessaires.");

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Accès Refusé",
                singleErrorMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // 403 Forbidden
    }

    // GESTION DES RESERVATIONS 
    @ExceptionHandler(UniteSoinsNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUniteSoinsNotFoundException(
            UniteSoinsNotFoundException ex, WebRequest request) {

        Map<String, String> singleErrorMap = Map.of(
                "id_unite_soins", ex.getMessage());

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Ressource Introuvable",
                singleErrorMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // 404 Not Found
    }

    @ExceptionHandler(LitIndisponibleException.class)
    public ResponseEntity<ErrorDetails> handleLitIndisponibleException(
            LitIndisponibleException ex, WebRequest request) {

        Map<String, String> singleErrorMap = Map.of(
                "disponibilite_lit", ex.getMessage());

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Conflit de Réservation",
                singleErrorMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409 Conflict
    }

// Gestion de l'erreur de type (ex: "ABCD" au lieu d'un nombre)
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, WebRequest request) {

        String parameterName = ex.getName();
        String typeAttendu = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "nombre";

        Map<String, String> errors = new HashMap<>();
        errors.put(parameterName, String.format("La valeur saisie est invalide. Le champ '%s' doit être un %s.",
                parameterName, typeAttendu));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Erreur de format de paramètre",
                errors,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

// Gestion des paramètres obligatoires manquants 
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getParameterName(), "Ce paramètre est obligatoire dans l'URL.");

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Paramètre manquant",
                errors,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MeadHead.Poc.exception.exeption_list.ValidationManuelleException.class)
    public ResponseEntity<Object> handleValidationManuelleException(
            MeadHead.Poc.exception.exeption_list.ValidationManuelleException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        org.springframework.validation.BindingResult result = ex.getBindingResult();

        //récupère les erreurs sur les champs classiques (@NotNull, @DecimalMin...)
        result.getFieldErrors().forEach(error
                -> errors.put(error.getField(), error.getDefaultMessage()));

        //récupère les erreurs globales (@OneOfAddressOrGps)
        result.getGlobalErrors().forEach(error -> {
            String key = (error.getCodes() != null && error.getCodes().length > 0)
                    ? error.getCodes()[0].substring(error.getCodes()[0].lastIndexOf('.') + 1)
                    : "choix_localisation";

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

    // GESTION DE L'ABSENCE DE LITS (NoBedAvailableException)
    @ExceptionHandler(MeadHead.Poc.exception.exeption_list.NoBedAvailableException.class)
    public ResponseEntity<ErrorDetails> handleNoBedAvailableException(
            MeadHead.Poc.exception.exeption_list.NoBedAvailableException ex, WebRequest request) {

        // On récupère la map d'erreurs passée à l'exception
        Map<String, String> errorsMap = ex.getErrors();

        // Si la map est vide ou nulle, on met un message par défaut
        if (errorsMap == null || errorsMap.isEmpty()) {
            errorsMap = Map.of("disponibilite", ex.getMessage());
        }

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Indisponibilité",
                errorsMap,
                request.getDescription(false));

        // On retourne un 404 car la ressource (un lit) est introuvable pour ces critères
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

// GESTION DES ERREURS GOOGLE / SERVICES EXTERNES
    @ExceptionHandler({
        MeadHead.Poc.exception.exeption_list.GoogleMapsServiceFailureException.class,
        MeadHead.Poc.exception.exeption_list.ExternalServiceFailureException.class
    })
    public ResponseEntity<ErrorDetails> handleGoogleMapsFailure(
            Exception ex, WebRequest request) {

        Map<String, String> errorsMap;

        if (ex instanceof MeadHead.Poc.exception.exeption_list.GoogleMapsServiceFailureException googleEx) {
            errorsMap = googleEx.getErrors();
        } else if (ex instanceof MeadHead.Poc.exception.exeption_list.ExternalServiceFailureException externalEx) {
            errorsMap = externalEx.getErrors();
        } else {
            errorsMap = Map.of("service_externe", ex.getMessage());
        }

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Erreur de localisation ou de trajet",
                errorsMap,
                request.getDescription(false));

        // On retourne 404 (Not Found) ou 502 (Bad Gateway) selon votre préférence
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        Map<String, String> errorsMap = Map.of(
                "format", "Le format de la donnée est invalide. Vérifiez que les nombres ne contiennent pas de texte ou de caractères spéciaux.");

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Format JSON invalide",
                errorsMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // GESTION GÉNÉRIQUE
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex, WebRequest request) {

        // Affiche la stack trace dans les logs du serveur
        ex.printStackTrace();

        // Dans la réponse envoyée au client, nous masquons les détails
        Map<String, String> singleErrorMap = Map.of(
                "detail", "Une erreur interne inconnue s'est produite.");

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Erreur interne du serveur (Non gérée)",
                singleErrorMap,
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal server
        // error
    }
}
