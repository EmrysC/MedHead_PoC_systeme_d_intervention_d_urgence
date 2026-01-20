package MeadHead.Poc.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import MeadHead.Poc.dto.SpecialisationDetailDTO;
import MeadHead.Poc.entites.GroupeSpecialite;
import MeadHead.Poc.entites.Specialisation;
import MeadHead.Poc.exception.exeption_list.GroupeSpecialiteMissingException;
import MeadHead.Poc.exception.exeption_list.SpecialisationNotFoundException;
import MeadHead.Poc.repository.SpecialisationRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
class SpecialisationServiceTest {

    @Mock
    private SpecialisationRepository specialisationRepository;

    @InjectMocks
    private SpecialisationService specialisationService;

    @Test
    @DisplayName("getSpecialisationDetailsDTO : Succès - Retourne le DTO complet")
    void getSpecialisationDetailsDTO_Success() {
        // Given
        Long id = 1L;
        GroupeSpecialite groupe = mock(GroupeSpecialite.class);
        when(groupe.getId()).thenReturn(10L);
        when(groupe.getNom()).thenReturn("Groupe Test");

        Specialisation spec = mock(Specialisation.class);
        when(spec.getId()).thenReturn(id);
        when(spec.getNom()).thenReturn("Spec Test");
        when(spec.getGroupeSpecialite()).thenReturn(groupe);

        when(specialisationRepository.findById(id)).thenReturn(Optional.of(spec));

        // When
        SpecialisationDetailDTO result = specialisationService.getSpecialisationDetailsDTO(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSpecialisationId()).isEqualTo(id);
        assertThat(result.getGroupeSpecialiteId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getSpecialisationDetailsDTO : Échec - Spécialisation introuvable (Exception 404)")
    void getSpecialisationDetailsDTO_NotFound() {
        // Given
        Long id = 1L;
        when(specialisationRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SpecialisationNotFoundException.class, ()
                -> specialisationService.getSpecialisationDetailsDTO(id)
        );
    }

    @Test
    @DisplayName("getSpecialisationDetailsDTO : Échec - Groupe manquant (Exception 500)")
    void getSpecialisationDetailsDTO_MissingGroup() {
        // Given
        Long id = 1L;
        Specialisation spec = mock(Specialisation.class);
        when(spec.getGroupeSpecialite()).thenReturn(null); // Branche : if (specialisation.getGroupeSpecialite() == null)

        when(specialisationRepository.findById(id)).thenReturn(Optional.of(spec));

        // When & Then
        assertThrows(GroupeSpecialiteMissingException.class, ()
                -> specialisationService.getSpecialisationDetailsDTO(id)
        );
    }
}
