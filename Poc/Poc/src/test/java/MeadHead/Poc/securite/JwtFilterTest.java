package MeadHead.Poc.securite;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import MeadHead.Poc.entites.User;
import MeadHead.Poc.enums.TypeDeRole;
import MeadHead.Poc.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JwtFilterTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    public void setUp() {
        // Isoler chaque test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        // Isoler chaque test (je l'ai aussi mis après car j'avais des problèmes avec certains tests)
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Succès : Token valide et utilisateur avec rôle ROLE_USER")
    void doFilterInternal_Success() throws ServletException, IOException {
        String token = "validToken";
        String email = "test@example.com";

        User userEntity = User.builder()
                .email(email)
                .role(TypeDeRole.ROLE_USER)
                .active(true)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractedEmail(token)).thenReturn(email);
        when(userService.loadUserByUsername(email)).thenReturn(userEntity);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Déjà authentifié : ignore le token (Couverture ligne getAuthentication() == null)")
    void doFilterInternal_AlreadyAuthenticated() throws ServletException, IOException {
        String token = "validToken";
        String email = "test@example.com";

        // On injecte manuellement une authentification dans le contexte pour rendre la condition FALSE
        UsernamePasswordAuthenticationToken existingAuth
                = new UsernamePasswordAuthenticationToken("alreadyIn", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractedEmail(token)).thenReturn(email);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // On vérifie que userService n'est JAMAIS appelé si on est déjà authentifié
        verify(userService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Email nul après extraction : ignore le chargement (Couverture ligne email != null)")
    void doFilterInternal_EmailExtractedIsNull() throws ServletException, IOException {
        String token = "tokenWithoutEmail";

        // Simule un token valide mais une extraction d'email qui renvoie null
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractedEmail(token)).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Vérifie que le userService n'est PAS appelé si l'email est null
        verify(userService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Pas de header Authorization : le filtre passe")
    void doFilterInternal_NoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtFilter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token expiré : ignore l'authentification")
    void doFilterInternal_ExpiredToken() throws ServletException, IOException {
        String token = "expired";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Utilisateur non trouvé : capture de l'exception")
    void doFilterInternal_UserNotFound() throws ServletException, IOException {
        String token = "valid";
        String email = "missing@test.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractedEmail(token)).thenReturn(email);
        when(userService.loadUserByUsername(email)).thenThrow(new UsernameNotFoundException("Not found"));

        jwtFilter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Utilisateur trouvé mais SANS autorités : l'authentification doit rester nulle (Couverture ligne 66)")
    void doFilterInternal_NoAuthorities() throws ServletException, IOException {
        String token = "validToken";
        String email = "test@example.com";

        // On force le rôle à null pour que getAuthorities() renvoie une liste vide
        User userEntity = User.builder()
                .email(email)
                .role(null)
                .active(true)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractedEmail(token)).thenReturn(email);
        when(userService.loadUserByUsername(email)).thenReturn(userEntity);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // L'authentification doit être nulle car authorities.isEmpty() est vrai
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
