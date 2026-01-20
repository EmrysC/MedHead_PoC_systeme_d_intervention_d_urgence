package MeadHead.Poc.controler;

import MeadHead.Poc.controller.ReservationController;
import MeadHead.Poc.dto.ReservationRequestDTO;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class) // Test uniquement la couche Web
@ActiveProfiles("pre_prod")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Pour convertir les objets en JSON

    @MockBean
    private ReservationService reservationService; // Mock du service

    @Test
    @DisplayName("POST /reservation/lit - 204 No Content : Succès de la réservation")
    void reserverLit_Succes() throws Exception {
        // GIVEN
        Long uniteSoinsId = 10L;
        ReservationRequestDTO dto = new ReservationRequestDTO();
        dto.setUniteSoinsId(uniteSoinsId);

        // On crée un faux utilisateur pour le @AuthenticationPrincipal
        User mockUser = new User();
        mockUser.setEmail("test@exemple.com");

        // WHEN & THEN
        mockMvc.perform(post("/reservation/lit")
                .with(csrf()) // Ajoute le token CSRF nécessaire en Spring Security
                .with(user(mockUser)) // Injecte l'utilisateur dans le SecurityContext
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        // Vérifie que le service a bien été appelé une fois avec les bons arguments
        verify(reservationService).reserverLit(eq(uniteSoinsId), any(User.class));
    }

    @Test
    @DisplayName("POST /reservation/lit - 400 Bad Request : DTO invalide")
    @WithMockUser // Simule un utilisateur connecté sans créer d'objet User spécifique
    void reserverLit_ValidationEchec() throws Exception {
        // GIVEN : Un DTO vide (en supposant que uniteSoinsId est @NotNull dans le DTO)
        ReservationRequestDTO dto = new ReservationRequestDTO();
        // uniteSoinsId n'est pas setté

        // WHEN & THEN
        mockMvc.perform(post("/reservation/lit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
