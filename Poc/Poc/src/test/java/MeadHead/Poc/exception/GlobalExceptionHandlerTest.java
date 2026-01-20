package MeadHead.Poc.exception;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;
import MeadHead.Poc.exception.exeption_list.ValidationManuelleException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        // Mock de la description de la requête pour ErrorDetails
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    @DisplayName("Validation Exceptions : Extraction des FieldErrors et GlobalErrors")
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("user", "email", "Email invalide");
        // Test de la logique de transformation du code (UserDTO.choix -> choix)
        ObjectError globalError = new ObjectError("user", new String[]{"UserDTO.choix"}, null, "Erreur globale");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getGlobalErrors()).thenReturn(List.of(globalError));

        ResponseEntity<Object> response = globalExceptionHandler.handleValidationExceptions(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorDetails details = (ErrorDetails) response.getBody();
        assertThat(details.getErrors()).containsEntry("email", "Email invalide");
        assertThat(details.getErrors()).containsEntry("choix", "Erreur globale");
    }

    @Test
    @DisplayName("Validation Manuelle : Doit utiliser le message de l'exception si pas d'erreurs")
    void testHandleValidationManuelleException_EmptyErrors() {
        ValidationManuelleException ex = mock(ValidationManuelleException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Message d'erreur manuel");

        ResponseEntity<Object> response = globalExceptionHandler.handleValidationManuelleException(ex, webRequest);

        ErrorDetails details = (ErrorDetails) response.getBody();
        assertThat(details.getErrors()).containsEntry("detail", "Message d'erreur manuel");
    }

    @Test
    @DisplayName("AuthenticationException : Cas avec cause réelle")
    void testHandleAuthenticationException() {
        AuthenticationException ex = mock(AuthenticationException.class);
        Exception cause = new RuntimeException("Échec réseau");
        when(ex.getCause()).thenReturn(cause);
        when(ex.getLocalizedMessage()).thenReturn("Identifiants incorrects");

        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleAuthenticationException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getErrors()).containsKey("RuntimeException");
    }

    @Test
    @DisplayName("AccessDeniedException : Vérification du statut 403")
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Interdit");
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleAccessDeniedException(ex, webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrors()).containsValue("Interdit");
    }

    @Test
    @DisplayName("Exceptions Métier : 404, 409 et Conflits")
    void testBusinessExceptions() {
        // UniteSoinsNotFoundException
        UniteSoinsNotFoundException usEx = new UniteSoinsNotFoundException(10L);
        ResponseEntity<ErrorDetails> res1 = globalExceptionHandler.handleUniteSoinsNotFoundException(usEx, webRequest);
        assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        // On vérifie que l'ID est bien présent dans la réponse comme configuré dans votre Handler
        assertThat(res1.getBody().getErrors()).containsKey("id_unite_soins");

        // LitIndisponibleException 
        LitIndisponibleException litEx = new LitIndisponibleException(1L);
        ResponseEntity<ErrorDetails> res2 = globalExceptionHandler.handleLitIndisponibleException(litEx, webRequest);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // EmailAlreadyExistsException 
        EmailAlreadyExistsException emailEx = new EmailAlreadyExistsException(Map.of("email", "Email déjà pris"));
        ResponseEntity<ErrorDetails> res3 = globalExceptionHandler.handleEmailAlreadyExistsException(emailEx, webRequest);
        assertThat(res3.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("HttpMessageNotReadableException : JSON malformé")
    void testHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleHttpMessageNotReadable(ex, webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Format JSON invalide");
    }

    @Test
    @DisplayName("Global Exception : Capture 500")
    void testHandleGlobalException() {
        Exception ex = new Exception("Erreur fatale");
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleGlobalException(ex, webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrors().get("detail")).isEqualTo("Une erreur interne est survenue.");
    }
}
