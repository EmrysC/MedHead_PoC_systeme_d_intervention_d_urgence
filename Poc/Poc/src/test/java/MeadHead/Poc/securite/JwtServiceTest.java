package MeadHead.Poc.securite;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import MeadHead.Poc.entites.User;
import MeadHead.Poc.enums.TypeDeRole;
import MeadHead.Poc.service.UserService;
import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("pre_prod")
class JwtServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtService jwtService;

    // Clé secrète de test (Base64 URL encoded)
    private final String TEST_SECRET = Base64.getUrlEncoder().encodeToString(
            "ma-super-cle-secrete-de-test-qui-doit-etre-longue".getBytes());
    private final long EXPIRATION = 3600000L; // 1 heure -> 3600000 ms

    @BeforeEach
    void setUp() {
        // Injection manuelle des @Value 
        ReflectionTestUtils.setField(jwtService, "JwtSecretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationTime", EXPIRATION);

        // Initialisation de la clé de signature (appel manuel du @PostConstruct)
        jwtService.init();
    }

    @Test
    @DisplayName("Génération de token : doit créer un JWT valide avec les claims de l'utilisateur")
    void testGenerateToken() {
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .nom("Jean")
                .prenom("Martin")
                .role(TypeDeRole.ROLE_USER)
                .active(true)
                .build();

        when(userService.loadUserByUsername(email)).thenReturn(user);

        Map<String, String> response = jwtService.generateToken(email);

        assertThat(response).containsKey("Bearer");
        String token = response.get("Bearer");
        assertThat(token).isNotBlank();

        // Vérification de l'extraction de l'email
        assertThat(jwtService.extractedEmail(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("isTokenExpired : doit retourner false pour un token récent")
    void testIsTokenExpired_False() {
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .nom("Doe")
                .prenom("John")
                .role(TypeDeRole.ROLE_USER)
                .active(true)
                .build();

        when(userService.loadUserByUsername(email)).thenReturn(user);

        String token = jwtService.generateToken(email).get("Bearer");

        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired : doit retourner true si les claims sont null (token invalide)")
    void testIsTokenExpired_NullClaims() {
        // Un token totalement invalide fera retourner null à extractAllClaims via les blocs catch
        String invalidToken = "invalid.token.value";

        boolean result = jwtService.isTokenExpired(invalidToken);

        assertThat(result).isTrue(); // Le code retourne true si expiration == null
    }

    @Test
    @DisplayName("extractClaim : doit retourner null si le token est mal formé (Couverture catch JwtException)")
    void testExtractClaim_InvalidToken() {
        String malformedToken = "not-a-jwt";

        String email = jwtService.extractedEmail(malformedToken);

        assertThat(email).isNull();
    }

    @Test
    @DisplayName("Couverture : Extraction avec un token expiré")
    void testExtractAllClaims_Expired() {
        // Configuration de l'expiration
        ReflectionTestUtils.setField(jwtService, "expirationTime", -1000L);
        jwtService.init();

        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .nom("Jean")
                .prenom("Martin")
                .role(TypeDeRole.ROLE_USER)
                .build();

        when(userService.loadUserByUsername(email)).thenReturn(user);

        String expiredToken = jwtService.generateToken(email).get("Bearer");

        assertThat(jwtService.extractedEmail(expiredToken)).isNull();
    }

    @Test
    @DisplayName("extractClaim : Couverture branche claims == null")
    void testExtractClaim_ClaimsNull() {
        // Appel de extractClaim avec un token nul ou invalide pour forcer la branche claims == null
        String result = jwtService.extractClaim("invalide", Claims::getSubject);

        assertThat(result).isNull();
    }
}
