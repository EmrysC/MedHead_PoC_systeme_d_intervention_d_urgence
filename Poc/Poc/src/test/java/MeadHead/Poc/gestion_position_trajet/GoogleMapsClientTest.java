package MeadHead.Poc.gestion_position_trajet;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import MeadHead.Poc.dto.PositionDTO;
import MeadHead.Poc.dto.TrajetResultatDTO;
import MeadHead.Poc.entites.Hopital;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.exception.exeption_list.GoogleMapsServiceFailureException;

@ExtendWith(MockitoExtension.class)
class GoogleMapsClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleMapsClient googleMapsClient;

    private final ObjectMapper mapper = new ObjectMapper();
    private Hopital hopitalTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleMapsClient, "googleApiKey", "test-api-key");
        ReflectionTestUtils.setField(googleMapsClient, "googleApiUrlTrajet", "http://maps.matrix");
        ReflectionTestUtils.setField(googleMapsClient, "googleApiUrlPositionGPS", "http://maps.geo");

        hopitalTest = Hopital.builder()
                .id(1L)
                .nom("Hôpital de Test")
                .build();
    }

    // --- TESTS : setPositionWithAdresse ---
    @Test
    @DisplayName("setPositionWithAdresse : Succès")
    void testSetPositionWithAdresse_Success() {
        PositionDTO position = new PositionDTO();
        position.setAddress("Paris");

        ObjectNode body = mapper.createObjectNode().put("status", "OK");
        body.putArray("results").addObject()
                .putObject("geometry").putObject("location")
                .put("lat", 48.8).put("lng", 2.3);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        googleMapsClient.setPositionWithAdresse(position);
        assertThat(position.getLatitude()).isEqualTo(48.8);
    }

    @Test
    @DisplayName("setPositionWithAdresse : Erreur si adresse vide")
    void testSetPositionWithAdresse_Empty() {
        PositionDTO position = new PositionDTO();
        position.setAddress("");
        assertThatThrownBy(() -> googleMapsClient.setPositionWithAdresse(position))
                .isInstanceOf(GoogleMapsServiceFailureException.class);
    }

    @Test
    @DisplayName("setPositionWithAdresse : Corps de réponse nul")
    void testSetPositionWithAdresse_NullBody() {
        PositionDTO position = new PositionDTO();
        position.setAddress("Paris");

        // Simulation d'une réponse avec un corps (body) null
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> googleMapsClient.setPositionWithAdresse(position))
                .isInstanceOf(GoogleMapsServiceFailureException.class)
                .satisfies(ex -> {
                    GoogleMapsServiceFailureException googleEx = (GoogleMapsServiceFailureException) ex;
                    // On vérifie que la Map contienne bien la clé geocoding_status avec la valeur PAS_DE_REPONSE
                    assertThat(googleEx.getErrors().get("geocoding_status"))
                            .contains("PAS_DE_REPONSE");
                });
    }

    // --- TESTS : calculeerTrajetsOptimises (avec branches de appelerDistanceMatrix) ---
    @Test
    @DisplayName("calculeerTrajetsOptimises : Succès coordonnés")
    void testCalculeerTrajets_Success() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        UniteSoins dest = createUniteSoins(48.1, 2.1, "Dest");

        ObjectNode body = createMatrixResponse("OK", 1000, 60);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        TrajetResultatDTO result = googleMapsClient.calculeerTrajetsOptimises(origin, List.of(dest));
        assertThat(result.getUnitesSoinsTrajets()).hasSize(1);
    }

    @Test
    @DisplayName("calculeerTrajetsOptimises : Erreur si origine nulle")
    void testCalculeerTrajets_InvalidOrigin() {
        PositionDTO origin = new PositionDTO();
        assertThatThrownBy(() -> googleMapsClient.calculeerTrajetsOptimises(origin, List.of()))
                .isInstanceOf(GoogleMapsServiceFailureException.class);
    }

    @Test
    @DisplayName("calculeerTrajetsOptimises : Erreur HTTP different de 200")
    void testCalculeerTrajets_HttpError() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> googleMapsClient.calculeerTrajetsOptimises(origin, List.of()))
                .isInstanceOf(GoogleMapsServiceFailureException.class);
    }

    @Test
    @DisplayName("calculeerTrajetsOptimises : Statut Google API invalide (ex: OVER_QUERY_LIMIT)")
    void testCalculeerTrajets_GoogleApiStatusError() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        ObjectNode body = mapper.createObjectNode().put("status", "OVER_QUERY_LIMIT");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        assertThatThrownBy(() -> googleMapsClient.calculeerTrajetsOptimises(origin, List.of()))
                .isInstanceOf(GoogleMapsServiceFailureException.class)
                .satisfies(ex -> assertThat(((GoogleMapsServiceFailureException) ex).getErrors()).containsKey("google_api_status"));
    }

    // --- TESTS : traiterReponseGoogle ---
    @Test
    @DisplayName("traiterReponseGoogle : Pas de 'rows' dans le JSON")
    void testTraiterReponse_NoRows() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        ObjectNode body = mapper.createObjectNode().put("status", "OK");
        // On ne met pas de "rows"

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        TrajetResultatDTO result = googleMapsClient.calculeerTrajetsOptimises(origin, List.of(createUniteSoins(48.1, 2.1, "A")));
        assertThat(result.getUnitesSoinsTrajets()).isEmpty();
    }

    @Test
    @DisplayName("traiterReponseGoogle : Un élément individuel n'est pas OK")
    void testTraiterReponse_ElementNotOk() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        UniteSoins dest = createUniteSoins(48.1, 2.1, "Dest");

        ObjectNode body = createMatrixResponse("NOT_FOUND", -1, -1);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        TrajetResultatDTO result = googleMapsClient.calculeerTrajetsOptimises(origin, List.of(dest));
        assertThat(result.getUnitesSoinsTrajets().get(0).getDestinationCalculee().isTrajetValide()).isFalse();
        assertThat(result.getUnitesSoinsTrajets().get(0).getDestinationCalculee().getDistanceMetres()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Global : Erreur de communication générique (RuntimeException)")
    void testCommunicationError() {
        PositionDTO origin = new PositionDTO("", 48.0, 2.0);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Timeout"));

        assertThatThrownBy(() -> googleMapsClient.calculeerTrajetsOptimises(origin, List.of()))
                .isInstanceOf(GoogleMapsServiceFailureException.class)
                .satisfies(ex -> assertThat(((GoogleMapsServiceFailureException) ex).getErrors()).containsKey("communication"));
    }

    // --- UTILS ---
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

    private ObjectNode createMatrixResponse(String elementStatus, long dist, long dur) {
        ObjectNode body = mapper.createObjectNode().put("status", "OK");
        ObjectNode element = mapper.createObjectNode().put("status", elementStatus);
        if (elementStatus.equals("OK")) {
            element.putObject("distance").put("value", dist);
            element.putObject("duration_in_traffic").put("value", dur);
        }
        body.putArray("rows").addObject().putArray("elements").add(element);
        return body;
    }
}
