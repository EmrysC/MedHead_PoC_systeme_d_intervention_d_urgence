package MeadHead.Poc.controler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import MeadHead.Poc.controller.UniteSoinsControler;
import MeadHead.Poc.dto.TrajetReponseDTO;
import MeadHead.Poc.exception.GlobalExceptionHandler;
import MeadHead.Poc.exception.exeption_list.ValidationManuelleException;
import MeadHead.Poc.repository.UniteSoinsRepository;
import MeadHead.Poc.service.UniteSoinsService;

@WebMvcTest(controllers = {UniteSoinsControler.class, GlobalExceptionHandler.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles("pre_prod")
@SuppressWarnings({"null", "unused"})
class UniteSoinsControlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UniteSoinsService uniteSoinsService;

    @MockitoBean
    private UniteSoinsRepository uniteSoinsRepository;

    @MockitoBean
    private Validator validator;

    @Test
    @DisplayName("GET /trajets - 200 Succès avec adresse")
    void rechercherTrajet_Succes_Adresse() throws Exception {
        when(uniteSoinsService.calculerTrajetReponse(any()))
                .thenReturn(TrajetReponseDTO.builder().build());

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "5")
                .param("adresse", "10 rue de Paris"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /trajets - 200 Succès avec GPS")
    void rechercherTrajet_Succes_Gps() throws Exception {
        when(uniteSoinsService.calculerTrajetReponse(any()))
                .thenReturn(TrajetReponseDTO.builder().build());

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "5")
                .param("latitude", "45.18")
                .param("longitude", "5.72"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /trajets - 400 Erreur : Exception métier personnalisée")
    void rechercherTrajet_Echec_CustomException() throws Exception {
        when(uniteSoinsService.calculerTrajetReponse(any()))
                .thenThrow(new ValidationManuelleException("Erreur de validation des arguments"));

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "1")
                .param("adresse", "Paris"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation manuelle"));
    }

    @Test
    @SuppressWarnings("null")
    @DisplayName("GET /trajets - 400 Erreur : Trop d'informations (Adresse + GPS)")
    void rechercherTrajet_Echec_TropInfos() throws Exception {
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.reject("conflit", "Veuillez fournir soit l'adresse, soit le GPS, mais pas les deux.");
            return null;
        }).when(validator).validate(any(), any(BindingResult.class));

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "1")
                .param("adresse", "Paris")
                .param("latitude", "45.1")
                .param("longitude", "5.7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings("null")
    @DisplayName("GET /trajets - 400 Erreur : Données de localisation manquantes")
    void rechercherTrajet_Echec_Manquant() throws Exception {
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.reject("manquant", "Données manquantes : adresse ou GPS requis.");
            return null;
        }).when(validator).validate(any(), any(BindingResult.class));

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings("null")
    @DisplayName("GET /trajets - 400 Erreur : Latitude hors bornes")
    void rechercherTrajet_Echec_LatitudeInvalide() throws Exception {
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.rejectValue("latitude", "DecimalMax", "La latitude doit être inférieure ou égale à 90.");
            return null;
        }).when(validator).validate(any(), any(BindingResult.class));

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "1")
                .param("latitude", "95.0")
                .param("longitude", "5.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.latitude").exists());
    }

    @Test
    @SuppressWarnings("null")
    @DisplayName("GET /trajets - 400 Erreur : Adresse vide")
    void rechercherTrajet_Echec_AdresseVide() throws Exception {
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.rejectValue("adresse", "Size", "L'adresse doit contenir entre 5 et 255 caractères.");
            return null;
        }).when(validator).validate(any(), any(BindingResult.class));

        mockMvc.perform(get("/unitesoins/trajets")
                .param("specialisationId", "5")
                .param("adresse", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.adresse").exists());
    }
}
