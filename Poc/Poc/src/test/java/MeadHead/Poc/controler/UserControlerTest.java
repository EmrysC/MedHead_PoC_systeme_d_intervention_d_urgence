package MeadHead.Poc.controler;

import MeadHead.Poc.controller.UserControler;
import MeadHead.Poc.dto.UserCreationDTO;
import MeadHead.Poc.dto.UserLoginDTO;
import MeadHead.Poc.securite.JwtService;
import MeadHead.Poc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserControler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("pre_prod")
class UserControlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    // --- TESTS POUR /user/connection ---
    @Test
    @DisplayName("POST /user/connection - 200 OK : Connexion réussie et retour du Token")
    void connection_Succes() throws Exception {
        // GIVEN
        UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "Password123!");
        Authentication mockAuth = mock(Authentication.class);

        // Simuler l'authentification réussie
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        // Simuler la génération du token
        when(jwtService.generateToken("test@example.com"))
                .thenReturn(Map.of("Bearer", "fake-jwt-token"));

        // WHEN & THEN
        mockMvc.perform(post("/user/connection")
                .with(csrf()) // Nécessaire si CSRF est activé
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Bearer").value("fake-jwt-token"));

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken("test@example.com");
    }

    @Test
    @DisplayName("POST /user/connection - 401 Unauthorized : Mauvais identifiants")
    void connection_Echec_IdentifiantsInvalides() throws Exception {
        // GIVEN
        UserLoginDTO loginDTO = new UserLoginDTO("wrong@example.com", "ValidPassword123!&&");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN & THEN
        mockMvc.perform(post("/user/connection")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
        // Note : Le status 401 est géré par Spring Security
    }

    // --- TESTS POUR /user/creation ---
    @Test
    @DisplayName("POST /user/creation - 200 OK : Création réussie")
    void creation_Succes() throws Exception {
        // GIVEN
        UserCreationDTO creationDTO = new UserCreationDTO();
        creationDTO.setEmail("newuser@example.com");
        creationDTO.setNom("Dupont");
        creationDTO.setPrenom("Jean");
        creationDTO.setPassword("Securite123!");

        // WHEN & THEN
        mockMvc.perform(post("/user/creation")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isOk());

        verify(userService).createUser(any(UserCreationDTO.class));
    }

    @Test
    @DisplayName("POST /user/creation - 400 Bad Request : Données manquantes")
    void creation_Echec_Validation() throws Exception {
        // GIVEN : DTO vide (si vous avez mis @NotBlank dans votre DTO)
        UserCreationDTO invalidDTO = new UserCreationDTO();

        // WHEN & THEN
        mockMvc.perform(post("/user/creation")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
}
