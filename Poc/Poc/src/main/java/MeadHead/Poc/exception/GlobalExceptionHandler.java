package MeadHead.Poc.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.EmailNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

        // Gestion des exceptions de Validation (@Valid)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Object> handleValidationExceptions(
                        MethodArgumentNotValidException ex, WebRequest request) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                ErrorDetails errorDetails = new ErrorDetails(
                                LocalDateTime.now(),
                                "Erreur de validation des arguments",
                                errors,
                                request.getDescription(false));

                return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // 400 bad request
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