package MeadHead.Poc.service;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

import MeadHead.Poc.dto.UserCreationDTO;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User testUser;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {

        userCreationDTO = new UserCreationDTO("test@example.com", "password_test",
                "Nom_test", "prenom_test");

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encrypted_password");
        testUser.setRole("ROLE_USER");
    }

    @Test
    void shouldCreateAnUserSuccessfully() {

        // Given : L'utilisateur n'existe pas en BDD
        when(userRepository.existsByEmail(userCreationDTO.getEmail())).thenReturn(
                false);
        // Simuler le cryptage
        when(bCryptPasswordEncoder.encode(userCreationDTO.getPassword())).thenReturn(
                "encrypted_password");
        // Simuler la sauvegarde
        when(userRepository.save(Mockito.any(User.class))).thenReturn(testUser);

        // When : Création
        User createdUser = userService.createUser(userCreationDTO);

        // Then : Vérification
        assertNotNull(createdUser);
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("encrypted_password", createdUser.getPassword());
        verify(userRepository, times(1)).save(Mockito.any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailIsAlreadyUsed() {

        // Given : L'utilisateur existe déjà en BDD
        when(userRepository.existsByEmail(userCreationDTO.getEmail())).thenReturn(
                true);

        // When / Then : L'exception spécifique est levée (409 CONFLICT)
        EmailAlreadyExistsException thrown = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(userCreationDTO);
        });

        // la Map d'erreurs a été correctement construite
        Map<String, String> errors = thrown.getErrors();
        assertFalse(errors.isEmpty());
        // On vérifie que la clé "email" (ou "detail") est présente et contient l'email
        assertTrue(errors.containsKey("email") || errors.containsKey("detail"));

        // Then : Aucun utilisateur n'est créé
        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(
                testUser));

        // When
        User foundUser = userService.loadUserByUsername(testUser.getEmail());

        // Then
        assertNotNull(foundUser);
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserIsNotPresent() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When / Then
        // Spring Security exige cette exception spécifique (UsernameNotFoundException)
        UsernameNotFoundException thrown = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(nonExistentEmail);
        });

        // On vérifie que le message précis de l'erreur est remonté
        assertTrue(thrown.getMessage().contains(nonExistentEmail),
                "Le message de l'exception doit mentionner l'email non trouvé.");

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

}