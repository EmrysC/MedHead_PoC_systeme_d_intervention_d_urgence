package meadhead.poc.gestion_position_trajet;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import meadhead.poc.dto.PositionDTO;
import meadhead.poc.dto.TrajetResultatDTO;
import meadhead.poc.entites.UniteSoins;
import meadhead.poc.exception.exeption_list.GoogleMapsServiceFailureException;

@Component
@Slf4j
public class GoogleMapsClient {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.api.url.trajet}")
    private String googleApiUrlTrajet;

    @Value("${google.api.url.positionGPS}")
    private String googleApiUrlPositionGPS;

    private static final String STATUS = "status";

    public GoogleMapsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TrajetResultatDTO calculeerTrajetsOptimises(PositionDTO positionDTO, List<UniteSoins> unitesDisponibles) {
        String destinations = unitesDisponibles.stream()
                .map(u -> u.getLatitude() + "," + u.getLongitude())
                .collect(Collectors.joining("|"));

        String origins = positionDTO.getPositionValid();
        if (origins == null) {
            throw new GoogleMapsServiceFailureException(Map.of("origine", "Position d'origine invalide."));
        }

        JsonNode response = appelerDistanceMatrix(
                origins,
                Objects.requireNonNull(destinations));

        if (response == null) {
            throw new GoogleMapsServiceFailureException(
                    Map.of("communication", "La réponse du service Google Maps est vide."));
        }

        if (response.path(STATUS).asText().equals("ZERO_RESULTS")) {
            return TrajetResultatDTO.builder()
                    .unitesSoinsTrajets(List.of())
                    .originePosition(positionDTO)
                    .build();
        }

        // Extraction de l'adresse normalisée
        JsonNode originAddresses = response.get("origin_addresses");
        if (originAddresses != null && originAddresses.has(0)) {
            String adresseNormalisee = originAddresses.get(0).asText();
            if (adresseNormalisee != null && !adresseNormalisee.trim().isEmpty()) {
                positionDTO.setAddress(adresseNormalisee);
            }
        }

        List<UniteeSoinsTrajetDTO> resultatsTrajet = traiterReponseGoogle(unitesDisponibles, response);

        return TrajetResultatDTO.builder()
                .unitesSoinsTrajets(resultatsTrajet)
                .originePosition(positionDTO)
                .build();
    }

    public void setPositionWithAdresse(@NonNull PositionDTO positionDTO) {
        String address = positionDTO.getAddress();

        if (address == null || address.trim().isEmpty()) {
            throw new GoogleMapsServiceFailureException(Map.of("adresse", "L'adresse fournie est vide."));
        }

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = String.format("%s?address=%s&key=%s", googleApiUrlPositionGPS, encodedAddress, googleApiKey);

        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    Objects.requireNonNull(url),
                    Objects.requireNonNull(HttpMethod.GET),
                    null,
                    JsonNode.class,
                    (Object) null);

            JsonNode body = responseEntity.getBody();

            if (body != null && body.path(STATUS).asText().equals("OK")) {
                JsonNode results = body.path("results");
                if (results.has(0)) {
                    JsonNode location = results.get(0).path("geometry").path("location");
                    positionDTO.setLatitude(location.path("lat").asDouble());
                    positionDTO.setLongitude(location.path("lng").asDouble());
                }
            } else {
                String status = (body != null) ? body.path(STATUS).asText() : "PAS_DE_REPONSE";
                throw new GoogleMapsServiceFailureException(
                        Map.of("geocoding_status", "Erreur Google Geocoding: " + status));
            }
        } catch (GoogleMapsServiceFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleMapsServiceFailureException(
                    Map.of("communication_geocoding", "Échec de la communication avec le service Google Maps."), e);
        }
    }

    private JsonNode appelerDistanceMatrix(@NonNull String origins, @NonNull String destinations) {

        // Encodage des paramètres pour l'URL HTTP, on remplace 
        String encodedOrigins = URLEncoder.encode(origins, StandardCharsets.UTF_8).replace("%2C", ",").replace("%7C", "|");
        String encodedDestinations = URLEncoder.encode(destinations, StandardCharsets.UTF_8).replace("%2C", ",").replace("%7C", "|");

        String url = String.format("%s?origins=%s&destinations=%s&units=metric&departure_time=now&traffic_model=optimistic&key=%s",
                googleApiUrlTrajet, encodedOrigins, encodedDestinations, googleApiKey);

        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    Objects.requireNonNull(url),
                    Objects.requireNonNull(HttpMethod.GET),
                    null,
                    JsonNode.class,
                    (Object) null);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                JsonNode body = responseEntity.getBody();

                if (body != null) {
                    String status = body.path(STATUS).asText();
                    // Si le statut n'est pas OK et n'est pas ZERO_RESULTS (qui est géré ailleurs)
                    if (!status.equals("OK") && !status.equals("ZERO_RESULTS")) {
                        throw new GoogleMapsServiceFailureException(
                                Map.of("google_api_status", "Erreur API Google : " + status));
                    }
                    log.debug("DEBUG GOOGLE : {}", body.toPrettyString());
                } else {
                    log.debug("DEBUG GOOGLE : body is null");
                }

                return body;
            } else {
                throw new GoogleMapsServiceFailureException(
                        Map.of("googleApi", "Erreur HTTP reçue de Google Maps: " + responseEntity.getStatusCode()));
            }
        } catch (GoogleMapsServiceFailureException e) {
            throw e; // FIX
        } catch (Exception e) {
            throw new GoogleMapsServiceFailureException(
                    Map.of("communication", "Échec de la communication avec le service Google Maps."), e);
        }
    }

    private List<UniteeSoinsTrajetDTO> traiterReponseGoogle(List<UniteSoins> unites, JsonNode response) {
        List<UniteeSoinsTrajetDTO> resultats = new ArrayList<>();
        JsonNode rows = response.path("rows");
        if (!rows.has(0)) {
            return resultats;
        }

        JsonNode elements = rows.get(0).path("elements");

        for (int i = 0; i < unites.size() && i < elements.size(); i++) {
            JsonNode element = elements.get(i);
            UniteSoins unitee = unites.get(i);

            boolean valide = element.path(STATUS).asText().equals("OK");
            long distanceMetres = valide ? element.path("distance").path("value").asLong(-1) : -1;
            long dureeSecondes = valide ? element.path("duration_in_traffic").path("value").asLong(-1) : -1;

            DestinationCalculeeDTO dc = new DestinationCalculeeDTO(
                    new PositionGPS(unitee.getLatitude().doubleValue(), unitee.getLongitude().doubleValue()),
                    unitee.getAdresse(), distanceMetres, dureeSecondes, valide);

            resultats.add(new UniteeSoinsTrajetDTO(unitee, dc));
        }
        return resultats;
    }
}
