package MeadHead.Poc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import MeadHead.Poc.dto.PositionDTO;
import MeadHead.Poc.dto.SpecialisationDetailDTO;
import MeadHead.Poc.dto.SpecialisationTrajetDTO;
import MeadHead.Poc.dto.TrajetResultatDTO;
import MeadHead.Poc.entites.Hopital;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.exception.exeption_list.ExternalServiceFailureException;
import MeadHead.Poc.exception.exeption_list.NoBedAvailableException;
import MeadHead.Poc.gestion_position_trajet.DestinationCalculeeDTO;
import MeadHead.Poc.gestion_position_trajet.GoogleMapsClient;
import MeadHead.Poc.gestion_position_trajet.UniteeSoinsTrajetDTO;
import MeadHead.Poc.repository.UniteSoinsRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UniteSoinsServiceTest {

    @InjectMocks
    private UniteSoinsService uniteSoinsService;

    @Mock
    private UniteSoinsRepository uniteSoinsRepository;

    @Mock
    private GoogleMapsClient googleMapsClient;

    @Mock
    private SpecialisationService specialisationService;

    private SpecialisationTrajetDTO inputDto;
    private UniteSoins uniteTest;
    private Hopital hopitalTest;

    @BeforeEach
    void setUp() {
        // Injection de la valeur @Value
        ReflectionTestUtils.setField(uniteSoinsService, "limitDestinations", 5);

        hopitalTest = Hopital.builder().id(1L).nom("Hôpital Central").build();

        inputDto = SpecialisationTrajetDTO.builder()
                .specialisationId(1L)
                .latitude(48.8566)
                .longitude(2.3522)
                .build();

        uniteTest = createUniteSoins(48.86, 2.34, "10 Rue de Paris");

        // Mock systématique des détails de spécialisation
        when(specialisationService.getSpecialisationDetailsDTO(anyLong()))
                .thenReturn(SpecialisationDetailDTO.builder()
                        .specialisationId(1L)
                        .specialisationNom("Cardiologie")
                        .build());
    }

    @Test
    @DisplayName("ERREUR 409 : Doit lancer NoBedAvailableException quand aucun lit n'est trouvé")
    void testNoBedAvailableException() {
        // Given : Le repository retourne une liste vide
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When / Then
        assertThatThrownBy(() -> uniteSoinsService.calculerTrajetReponse(inputDto))
                .isInstanceOf(NoBedAvailableException.class)
                .hasMessageContaining("Aucun lit disponible");

        // Vérification : On ne va pas plus loin (pas d'appel Google Maps)
        verifyNoInteractions(googleMapsClient);
    }

    @Test
    @DisplayName("ERREUR 503 : Doit lancer ExternalServiceFailureException si aucun itinéraire n'est valide")
    void testExternalServiceFailure() {
        // Given : On a des lits, mais Google ne trouve pas de route valide
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(anyLong(), anyInt()))
                .thenReturn(List.of(uniteTest));

        DestinationCalculeeDTO invalidDest = DestinationCalculeeDTO.builder().trajetValide(false).build();
        TrajetResultatDTO apiRes = TrajetResultatDTO.builder()
                .unitesSoinsTrajets(List.of(new UniteeSoinsTrajetDTO(uniteTest, invalidDest)))
                .build();

        when(googleMapsClient.calculeerTrajetsOptimises(any(), any())).thenReturn(apiRes);

        // When / Then
        assertThatThrownBy(() -> uniteSoinsService.calculerTrajetReponse(inputDto))
                .isInstanceOf(ExternalServiceFailureException.class)
                .hasMessageContaining("Le service de calcul de trajets optimisés est indisponible ou a échoué");
    }

    @Test
    @DisplayName("SUCCÈS : dataset > limite (vérifie le tri vol d'oiseau et la limite)")
    void testLargeDataset() {
        // Given : 8 unités disponibles (supérieur à la limite de 5)
        List<UniteSoins> manyUnits = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            manyUnits.add(createUniteSoins(48.0 + i, 2.0 + i, "Adresse " + i));
        }

        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(anyLong(), anyInt()))
                .thenReturn(manyUnits);

        mockGoogleMapsSuccess(5);

        // When
        uniteSoinsService.calculerTrajetReponse(inputDto);

        // Then : Vérifie que Google Maps n'a reçu que les 5 destinations les plus proches
        verify(googleMapsClient).calculeerTrajetsOptimises(any(), argThat(list -> list.size() == 5));
    }

    @Test
    @DisplayName("trouverParId : doit retourner l'entité si elle existe")
    void testTrouverParId() {
        when(uniteSoinsRepository.findById(10L)).thenReturn(Optional.of(uniteTest));
        UniteSoins result = uniteSoinsService.trouverParId(10L);
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("ERREUR 409 : Doit lancer NoBedAvailableException avec les détails de spécialisation")
    void testNoBedAvailableException_ShouldThrowConflict() {
        // GIVEN
        Long specialisationId = 1L;
        SpecialisationTrajetDTO input = SpecialisationTrajetDTO.builder()
                .specialisationId(specialisationId)
                .latitude(48.8566)
                .longitude(2.3522)
                .build();

        // On simule qu'aucun lit n'est trouvé en base de données
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(eq(specialisationId), eq(0)))
                .thenReturn(Collections.emptyList());

        // WHEN / THEN
        assertThatThrownBy(() -> uniteSoinsService.calculerTrajetReponse(input))
                .isInstanceOf(NoBedAvailableException.class)
                .hasMessageContaining("Aucun lit disponible pour cette spécialisation.");

        // On s'assure que le GoogleMapsClient n'est JAMAIS appelé
        // Pour économiser des appels API inutiles 
        verifyNoInteractions(googleMapsClient);

        // On vérifie aussi que le repo a bien été sollicité avec les bons paramètres
        verify(uniteSoinsRepository).findBySpecialisationIdAndLitsDisponiblesGreaterThan(specialisationId, 0);
    }

    // --- UTILITAIRES ---
    private void mockGoogleMapsSuccess(int size) {
        List<UniteeSoinsTrajetDTO> trajets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            DestinationCalculeeDTO dest = DestinationCalculeeDTO.builder()
                    .trajetValide(true)
                    .distanceMetres(1000L * (i + 1))
                    .build();
            trajets.add(new UniteeSoinsTrajetDTO(uniteTest, dest));
        }

        TrajetResultatDTO result = TrajetResultatDTO.builder()
                .unitesSoinsTrajets(trajets)
                .originePosition(new PositionDTO())
                .build();

        when(googleMapsClient.calculeerTrajetsOptimises(any(), any())).thenReturn(result);
    }

    private UniteSoins createUniteSoins(Double lat, Double lng, String addr) {
        return UniteSoins.builder()
                .id(10L)
                .latitude(java.math.BigDecimal.valueOf(lat))
                .longitude(java.math.BigDecimal.valueOf(lng))
                .adresse(addr)
                .litsDisponibles(5)
                .hopital(hopitalTest)
                .build();
    }
}
