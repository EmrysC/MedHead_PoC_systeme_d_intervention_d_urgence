package MeadHead.Poc.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import MeadHead.Poc.Gestion_position_trajet.DestinationCalculeeDTO;
import MeadHead.Poc.Gestion_position_trajet.GoogleMapsClient;
import MeadHead.Poc.Gestion_position_trajet.UniteeSoinsTrajetDTO;
import MeadHead.Poc.dto.PositionDTO;
import MeadHead.Poc.dto.SpecialisationDetailDTO;
import MeadHead.Poc.dto.SpecialisationTrajetDTO;
import MeadHead.Poc.dto.TrajetReponseDTO;
import MeadHead.Poc.dto.TrajetResultatDTO;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.exception.exeption_list.ExternalServiceFailureException;
import MeadHead.Poc.exception.exeption_list.NoBedAvailableException;
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

    @BeforeEach
    @SuppressWarnings("null")
    public void setUp() {
        // Injection de la valeur @Value (limit-destinations) qui n'est pas gérée par Mockito
        ReflectionTestUtils.setField(uniteSoinsService, "limitDestinations", 5);

        inputDto = SpecialisationTrajetDTO.builder()
                .specialisationId(1L)
                .latitude(45.0)
                .longitude(5.0)
                .build();

        uniteTest = UniteSoins.builder()
                .id(10L)
                .litsDisponibles(5)
                .latitude(new BigDecimal("45.1"))
                .longitude(new BigDecimal("5.1"))
                .adresse("Adresse Test")
                .build();
    }

    @Test
    void shouldReturnTrajetReponseSuccessfully() {
        // Given
        List<UniteSoins> dispo = List.of(uniteTest);
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(1L, 0))
                .thenReturn(dispo);

        when(specialisationService.getSpecialisationDetailsDTO(1L))
                .thenReturn(SpecialisationDetailDTO.builder()
                        .specialisationNom("Cardio")
                        .build());

        // Simulation de la réponse Google Maps
        DestinationCalculeeDTO dest = new DestinationCalculeeDTO();
        dest.setTrajetValide(true);
        dest.setDistanceMetres(1000L);

        UniteeSoinsTrajetDTO trajetDTO = UniteeSoinsTrajetDTO.builder()
                .idUniteSoins(10L)
                .destinationCalculee(dest)
                .build();

        TrajetResultatDTO resultatAPI = TrajetResultatDTO.builder()
                .unitesSoinsTrajets(List.of(trajetDTO))
                .build();

        when(googleMapsClient.calculeerTrajetsOptimises(any(PositionDTO.class), anyList()))
                .thenReturn(resultatAPI);

        // When
        TrajetReponseDTO response = uniteSoinsService.calculerTrajetReponse(inputDto);

        // Then
        assertNotNull(response);
        assertEquals("Cardio", response.getSpecialisationDetailDTO().getSpecialisationNom());
        assertFalse(response.getTrajetsCalculesValide().getUnitesSoinsTrajets().isEmpty());
        verify(googleMapsClient, times(1)).calculeerTrajetsOptimises(any(), any());
    }

    @Test
    void shouldThrowNoBedAvailableExceptionWhenNoBedsFound() {
        // Given
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(1L, 0))
                .thenReturn(new ArrayList<>());

        // When / Then
        // On stocke le résultat dans 'exception' pour que l'IDE ne dise plus qu'il est "ignoré"
        NoBedAvailableException exception = assertThrows(NoBedAvailableException.class, () -> {
            uniteSoinsService.calculerTrajetReponse(inputDto);
        });

        verify(googleMapsClient, never()).calculeerTrajetsOptimises(any(), any());
    }

    @Test
    void shouldThrowExternalServiceFailureExceptionWhenNoValidItineraryFound() {
        // Given
        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(1L, 0))
                .thenReturn(List.of(uniteTest));

        // Simulation d'un trajet retourné par Google mais marqué comme invalide (ex: route coupée)
        DestinationCalculeeDTO invalidDest = new DestinationCalculeeDTO();
        invalidDest.setTrajetValide(false);

        UniteeSoinsTrajetDTO trajetInvalide = UniteeSoinsTrajetDTO.builder()
                .destinationCalculee(invalidDest)
                .build();

        TrajetResultatDTO resultatAPI = TrajetResultatDTO.builder()
                .unitesSoinsTrajets(List.of(trajetInvalide))
                .build();

        when(googleMapsClient.calculeerTrajetsOptimises(any(), any()))
                .thenReturn(resultatAPI);

        // When / Then
        assertThrows(ExternalServiceFailureException.class, () -> {
            uniteSoinsService.calculerTrajetReponse(inputDto);
        });
    }

    @Test
    void shouldHandleLargeListOfUnitsByFilteringWithVolOiseau() {
        // On crée 10 unités alors que la limite est 5
        List<UniteSoins> manyUnits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyUnits.add(UniteSoins.builder()
                    .id((long) i)
                    .latitude(new BigDecimal(45.0 + i / 10))
                    .longitude(new BigDecimal(5.0 + i / 10))
                    .litsDisponibles(1)
                    .build());
        }

        when(uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(1L, 0))
                .thenReturn(manyUnits);

        // On simule une réponse vide de Google pour s'arrêter là dans le test
        when(googleMapsClient.calculeerTrajetsOptimises(any(), any()))
                .thenReturn(TrajetResultatDTO.builder().unitesSoinsTrajets(new ArrayList<>()).build());

        // When
        try {
            uniteSoinsService.calculerTrajetReponse(inputDto);
        } catch (ExternalServiceFailureException e) {
            // C'est normal ici car on a mocké une liste vide
        }

        // Then : On vérifie que GoogleMapsClient n'a reçu que 5 destinations (la limite)
        verify(googleMapsClient).calculeerTrajetsOptimises(any(), argThat(list -> list.size() == 5));
    }
}
