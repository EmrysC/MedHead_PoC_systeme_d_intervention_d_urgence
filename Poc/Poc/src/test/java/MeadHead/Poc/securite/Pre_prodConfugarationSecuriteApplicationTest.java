package MeadHead.Poc.securite;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import MeadHead.Poc.config.PasswordConfig;
import MeadHead.Poc.controller.UserControler;
import MeadHead.Poc.service.UserService;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.enums.TypeDeRole;
import MeadHead.Poc.dto.UserCreationDTO;

@WebMvcTest(UserControler.class)
@Import({Pre_prodConfugarationSecuriteApplication.class, PasswordConfig.class})
@ActiveProfiles("pre_prod")
class Pre_prodConfugarationSecuriteApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

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
