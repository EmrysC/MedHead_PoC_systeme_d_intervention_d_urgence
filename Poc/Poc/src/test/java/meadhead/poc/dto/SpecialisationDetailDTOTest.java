package meadhead.poc.dto;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;

import meadhead.poc.entites.GroupeSpecialite;
import meadhead.poc.entites.Specialisation;

@JsonTest
@ActiveProfiles("dev")
class SpecialisationDetailDTOTest {

    @Autowired
    private JacksonTester<SpecialisationDetailDTO> json;

    @Test
    @DisplayName("Constructeur : Doit mapper correctement les données de l'entité Specialisation")
    void testConstructorFromEntity() {
        // Préparation des mocks pour l'entité et son groupe
        Specialisation specialisation = mock(Specialisation.class);
        GroupeSpecialite groupe = mock(GroupeSpecialite.class);

        // Définition des comportements attendus
        when(specialisation.getId()).thenReturn(10L);
        when(specialisation.getNom()).thenReturn("Cardiologie Interventionnelle");
        when(specialisation.getGroupeSpecialite()).thenReturn(groupe);
        when(groupe.getId()).thenReturn(1L);
        when(groupe.getNom()).thenReturn("Cardiologie");

        // Exécution du constructeur personnalisé
        SpecialisationDetailDTO dto = new SpecialisationDetailDTO(specialisation);

        // Vérifications de la logique de mapping
        assertThat(dto.getSpecialisationId()).isEqualTo(10L);
        assertThat(dto.getSpecialisationNom()).isEqualTo("Cardiologie Interventionnelle");
        assertThat(dto.getGroupeSpecialiteId()).isEqualTo(1L);
        assertThat(dto.getGroupeSpecialiteNom()).isEqualTo("Cardiologie");
    }

    @Test
    @DisplayName("Lombok : Vérification du Builder et des Getters/Setters")
    void testLombokFeatures() {
        // Test du Builder
        SpecialisationDetailDTO dto = SpecialisationDetailDTO.builder()
                .specialisationId(5L)
                .specialisationNom("Neurologie")
                .groupeSpecialiteId(2L)
                .groupeSpecialiteNom("Médecine")
                .build();

        assertThat(dto.getSpecialisationId()).isEqualTo(5L);
        assertThat(dto.getSpecialisationNom()).isEqualTo("Neurologie");

        // Test des Setters
        dto.setSpecialisationNom("Nouveau Nom");
        assertThat(dto.getSpecialisationNom()).isEqualTo("Nouveau Nom");
    }

    @Test
    @DisplayName("JSON : Vérification de la sérialisation des champs")
    void testSerialization() throws Exception {
        SpecialisationDetailDTO dto = new SpecialisationDetailDTO();
        dto.setSpecialisationId(1L);
        dto.setSpecialisationNom("Test Spé");
        dto.setGroupeSpecialiteId(100L);
        dto.setGroupeSpecialiteNom("Test Groupe");

        // Vérifie que les noms des propriétés JSON correspondent aux attributs
        assertThat(json.write(dto)).hasJsonPathNumberValue("@.specialisationId", 1);
        assertThat(json.write(dto)).hasJsonPathStringValue("@.specialisationNom", "Test Spé");
        assertThat(json.write(dto)).hasJsonPathNumberValue("@.groupeSpecialiteId", 100);
        assertThat(json.write(dto)).hasJsonPathStringValue("@.groupeSpecialiteNom", "Test Groupe");
    }

    @Test
    @DisplayName("Constructeur : Mapping depuis l'entité")
    void testMappingEntity() {
        Specialisation spec = mock(Specialisation.class);
        GroupeSpecialite groupe = mock(GroupeSpecialite.class);
        when(spec.getGroupeSpecialite()).thenReturn(groupe);
        when(groupe.getId()).thenReturn(1L);

        SpecialisationDetailDTO dto = new SpecialisationDetailDTO(spec);
        assertThat(dto.getGroupeSpecialiteId()).isEqualTo(1L);
    }
}
