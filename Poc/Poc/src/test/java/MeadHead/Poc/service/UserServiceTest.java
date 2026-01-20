package MeadHead.Poc.service;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import MeadHead.Poc.dto.UserCreationDTO;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.enums.TypeDeRole;
import MeadHead.Poc.exception.exeption_list.EmailAlreadyExistsException;
import MeadHead.Poc.exception.exeption_list.EmailNotFoundException;
import MeadHead.Poc.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(
                "test@example.com",
                "password_test",
                "Nom_test",
                "prenom_test"
        );

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encrypted_password");
        testUser.setRole(TypeDeRole.ROLE_USER);
    }

    // --- TESTS POUR createUser ---
    @Test
    @DisplayName("createUser : Succès de la création")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(userCreationDTO.getEmail())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(userCreationDTO.getPassword())).thenReturn("encrypted_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(userCreationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser : Échec si l'email existe déjà (EmailAlreadyExistsException)")
    void shouldThrowEmailAlreadyExistsException_WhenEmailIsTaken() {
        // Given
        when(userRepository.existsByEmail(userCreationDTO.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> userService.createUser(userCreationDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Un utilisatuer existe deja pour cet email");

        verify(userRepository, never()).save(any(User.class));
    }

    // --- TESTS POUR loadUserByUsername ---
    @Test
    @DisplayName("loadUserByUsername : Succès du chargement")
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userService.loadUserByUsername("test@example.com");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("loadUserByUsername : Échec et jet de UsernameNotFoundException")
    void shouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Given
        String email = "unknown@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(email);
    }

    // --- TEST POUR LA COUVERTURE DE L'EXCEPTION EmailNotFoundException ---
    @Test
    @DisplayName("EmailNotFoundException : Couverture directe de l'objet exception")
    void testEmailNotFoundExceptionCoverage() {
        // Ce test assure que la classe d'exception elle-même est couverte à 100%
        // même si le service lance actuellement une UsernameNotFoundException
        Map<String, String> errors = Map.of("email", "erreur");
        EmailNotFoundException exception = new EmailNotFoundException(errors);

        assertThat(exception.getMessage()).isEqualTo("Conflit de données : Email inexistant.");
    }
}
