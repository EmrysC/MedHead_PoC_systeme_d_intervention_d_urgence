package MeadHead.Poc.controler;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import MeadHead.Poc.controller.GroupeSpecialiteController;
import MeadHead.Poc.dto.SpecialisationGroupeDTO;
import MeadHead.Poc.exception.GlobalExceptionHandler;
import MeadHead.Poc.service.GroupeSpecialiteService;

@WebMvcTest(controllers = {GroupeSpecialiteController.class, GlobalExceptionHandler.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles("pre_prod")
class GroupeSpecialiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupeSpecialiteService groupeSpecialiteService;

    @Test
    @DisplayName("GET /specilites - 200 Succès : Retourne la liste des groupes")
    void getSpecialitesGroupes_Succes() throws Exception {
        // Préparation des données de test (basé sur ton exemple Swagger)
        SpecialisationGroupeDTO cardio = new SpecialisationGroupeDTO();
        cardio.setId(1L);
        cardio.setNom("Cardiologie");

        SpecialisationGroupeDTO neuro = new SpecialisationGroupeDTO();
        neuro.setId(2L);
        neuro.setNom("Neurologie");

        List<SpecialisationGroupeDTO> mockResponse = Arrays.asList(cardio, neuro);

        // Mock du service
        when(groupeSpecialiteService.getAllSpecialiteGroupesAsDTO()).thenReturn(mockResponse);

        // Exécution et vérification
        mockMvc.perform(get("/specilites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Cardiologie"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nom").value("Neurologie"));
    }

    @Test
    @DisplayName("GET /specilites - 200 Succès : Liste vide")
    void getSpecialitesGroupes_Vide() throws Exception {
        when(groupeSpecialiteService.getAllSpecialiteGroupesAsDTO()).thenReturn(List.of());

        mockMvc.perform(get("/specilites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
