package MeadHead.Poc.securite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import MeadHead.Poc.config.PasswordConfig;
import MeadHead.Poc.controller.UserControler;
import MeadHead.Poc.dto.UserCreationDTO;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.enums.TypeDeRole;
import MeadHead.Poc.service.UserService;

@WebMvcTest(UserControler.class)
@Import({Pre_prodConfugarationSecuriteApplication.class, PasswordConfig.class})
@ActiveProfiles("pre_prod")
@SuppressWarnings({"null", "unused"})
class Pre_prodConfugarationSecuriteApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Règle permitAll() : Accès autorisé (Identique à l'exécution réelle)")
    void testPublicEndpoints() throws Exception {
        // On crée un utilisateur simulé complet pour que le JwtService ne plante pas
        User mockUser = User.builder()
                .id(1L)
                .email("utilisayteur.non@existant1.com")
                .nom("paul")
                .prenom("martin")
                .role(TypeDeRole.ROLE_USER) // Indispensable pour les claims JWT
                .build();

        //  On configure le mock pour renvoyer cet utilisateur
        when(userService.createUser(any(UserCreationDTO.class))).thenReturn(mockUser);

        //  Les données JSON exactes de votre log
        String userJson = """
        {
            "email": "utilisayteur.non@existant1.com",
            "password": "MotDePasseSecret123&",
            "nom": "paul",
            "prenom" : "martin"
        }
        """;

        mockMvc.perform(post("/user/creation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Règle authenticated() : Accès refusé sur route protégée")
    void testProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/specilites"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Règle denyAll() : Route inconnue bloquée")
    void testAnyOtherRequest() throws Exception {
        mockMvc.perform(get("/api/route/inexistante"))
                .andExpect(status().isForbidden());
    }
}
