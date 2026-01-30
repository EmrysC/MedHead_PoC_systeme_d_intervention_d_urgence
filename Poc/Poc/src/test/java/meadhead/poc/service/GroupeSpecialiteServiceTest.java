package meadhead.poc.service;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import meadhead.poc.dto.SpecialisationGroupeDTO;
import meadhead.poc.dto.SpecialisationOptionDTO;
import meadhead.poc.entites.GroupeSpecialite;
import meadhead.poc.entites.Specialisation;
import meadhead.poc.repository.GroupeSpecialiteRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
class GroupeSpecialiteServiceTest {

    @Mock
    private GroupeSpecialiteRepository groupeRepository;

    @InjectMocks
    private GroupeSpecialiteService groupeSpecialiteService;

    @Test
    @DisplayName("getAllSpecialiteGroupesAsDTO : Doit mapper et trier les groupes et options")
    void getAllSpecialiteGroupesAsDTO_ShouldMapAndSortCorrectly() {
        // --- GIVEN : Préparation de données non triées ---

        // Groupe B avec des spécialisations Z et Y (ordre inverse)
        Specialisation specZ = new Specialisation();
        specZ.setId(10L);
        specZ.setNom("Zurgie");
        Specialisation specY = new Specialisation();
        specY.setId(11L);
        specY.setNom("Ypnotisme");

        GroupeSpecialite groupeB = new GroupeSpecialite();
        groupeB.setId(2L);
        groupeB.setNom("B-Groupe");
        groupeB.setSpecialisations(Arrays.asList(specZ, specY));

        // Groupe A
        GroupeSpecialite groupeA = new GroupeSpecialite();
        groupeA.setId(1L);
        groupeA.setNom("A-Groupe");
        groupeA.setSpecialisations(List.of());

        // Simulation du repository retournant B avant A
        when(groupeRepository.findAll()).thenReturn(Arrays.asList(groupeB, groupeA));

        // --- WHEN ---
        List<SpecialisationGroupeDTO> result = groupeSpecialiteService.getAllSpecialiteGroupesAsDTO();

        // --- THEN : Vérifications du tri et du mapping ---
        assertThat(result).hasSize(2);

        // Vérification du tri des groupes (A doit être avant B)
        assertThat(result.get(0).getNom()).isEqualTo("A-Groupe");
        assertThat(result.get(1).getNom()).isEqualTo("B-Groupe");

        // Vérification du tri des options dans le groupe B (Y avant Z)
        List<SpecialisationOptionDTO> optionsB = result.get(1).getSpecialisations();
        assertThat(optionsB).hasSize(2);
        assertThat(optionsB.get(0).getNom()).isEqualTo("Ypnotisme");
        assertThat(optionsB.get(1).getNom()).isEqualTo("Zurgie");

        //  Vérification de la conversion de l'ID en String
        assertThat(optionsB.get(0).getId()).isEqualTo("11");
    }

    @Test
    @DisplayName("clearSpecialitesCache : Doit s'exécuter sans erreur")
    void clearSpecialitesCache_ShouldExecute() {
        // Cette méthode ne fait qu'un print, on vérifie qu'elle ne lève pas d'exception
        groupeSpecialiteService.clearSpecialitesCache();
        // Le test passe si aucune exception n'est jetée
    }
}
