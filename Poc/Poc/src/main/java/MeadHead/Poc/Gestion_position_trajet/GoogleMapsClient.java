package MeadHead.Poc.Gestion_position_trajet;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import MeadHead.Poc.dto.PositionDTO;
import MeadHead.Poc.dto.TrajetResultatDTO;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.exception.exeption_list.GoogleMapsServiceFailureException;

//https://developers.google.com/maps/documentation/distance-matrix/overview?hl=fr
//https://developers.google.com/maps/documentation/distance-matrix/distance-matrix?hl=fr#maps_http_distancematrix_latlng-sh
@Component
public class GoogleMapsClient {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.api.url}")
    private String googleApiUrl;

    public GoogleMapsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /*
    public List<UniteeSoinsTrajetDTO> calculeerTrajetsOptimises(PositionDTO positionDTO,
            List<UniteSoins> unitesDisponibles) {

        String destinations = unitesDisponibles.stream()
                .map(u -> u.getLatitude() + "," + u.getLongitude())
                .collect(Collectors.joining("|"));

        JsonNode response = appelerDistanceMatrix(positionDTO.getPositionValid(), destinations);

        if (response == null || response.at("/status").asText().equals("ZERO_RESULTS")) {
            return List.of();
        }

        return traiterReponseGoogle(unitesDisponibles, response);

    }  */
    public TrajetResultatDTO calculeerTrajetsOptimises(PositionDTO positionDTO,
            List<UniteSoins> unitesDisponibles) {

        // concaténation des destinations
        String destinations = unitesDisponibles.stream()
                .map(u -> u.getLatitude() + "," + u.getLongitude())
                .collect(Collectors.joining("|"));

        // Appel à l'API Google Maps
        JsonNode response = appelerDistanceMatrix(positionDTO.getPositionValid(), destinations);

        if (response == null || response.at("/status").asText().equals("ZERO_RESULTS")) {
            // Retourne un DTO vide en cas d'échec ou de résultats nuls
            return TrajetResultatDTO.builder()
                    .unitesSoinsTrajets(List.of())
                    .originePosition(positionDTO) // Retourne au moins la position d'origine fournie
                    .build();
        }

        // Extraction de l'adresse normalisée de Google
        String adresseNormalisee = response.get("origin_addresses").get(0).asText();
        if (adresseNormalisee != null && !adresseNormalisee.trim().isEmpty()) {
            positionDTO.setAddress(adresseNormalisee);
        }

        // Traitement des résultats de trajet pour les destinations
        List<UniteeSoinsTrajetDTO> resultatsTrajet = traiterReponseGoogle(unitesDisponibles, response);

        // Construction et retour du TrajetResultatDTO
        return TrajetResultatDTO.builder()
                .unitesSoinsTrajets(resultatsTrajet)
                .originePosition(positionDTO) // Utilise l'objet positionDTO mis à jour
                .build();
    }

    private JsonNode appelerDistanceMatrix(String origins, String destinations) {

        // Encodage
        String encodedOrigins = URLEncoder.encode(origins, StandardCharsets.UTF_8);
        String encodedDestinations = URLEncoder.encode(destinations, StandardCharsets.UTF_8);
        encodedOrigins = encodedOrigins.replace("%2C", ",");
        encodedDestinations = encodedDestinations.replace("%2C", ",");
        encodedOrigins = encodedOrigins.replace("%7C", "|");
        encodedDestinations = encodedDestinations.replace("%7C", "|");

// Construction url
        String url = googleApiUrl
                + "?origins=" + encodedOrigins
                + "&destinations=" + encodedDestinations
                + "&units=metric"
                + "&departure_time=now"
                + "&traffic_model=optimistic"
                + "&key=" + googleApiKey;

        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    JsonNode.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                JsonNode body = responseEntity.getBody();

                if (body != null) {
                    String rootStatus = body.path("status").asText();

                    // Si le statut n'est pas OK ou ZERO_RESULTS
                    if (!rootStatus.equals("OK") && !rootStatus.equals("ZERO_RESULTS")) {
                        String errorMsg = body.path("error_message").asText("Pas de message détaillé");
                        throw new GoogleMapsServiceFailureException(
                                Map.of("google_api_status", "Erreur Google: " + rootStatus + " - " + errorMsg)
                        );
                    }
                }

                return body;
            } else {

                String errorMessage = String.format("Erreur HTTP reçue de Google Maps: %s. URL: %s",
                        responseEntity.getStatusCode(), url);

                // Lever l'exception spécifique pour les erreurs de statut HTTP (non 2xx)
                // Le Map.of est correct pour passer les détails de l'erreur.
                throw new GoogleMapsServiceFailureException(
                        Map.of("googleApi", errorMessage));
            }

        } catch (Exception e) {
            // Échec de la communication (connexion, timeout, JSON invalide, etc.)
            String errorMessage = String.format("Erreur lors de l'appel à l'API Google Maps: %s. Message: %s. URL: %s",
                    e.getClass().getName(), e.getMessage(), url);

            // Lever l'exception spécifique en encapsulant la cause
            throw new GoogleMapsServiceFailureException(
                    Map.of("communication", errorMessage), e);
        }
    }

    private List<UniteeSoinsTrajetDTO> traiterReponseGoogle(List<UniteSoins> unites, JsonNode response) {

        List<UniteeSoinsTrajetDTO> resultats = new ArrayList<>();

        JsonNode elementsRow = response.get("rows").get(0);
        JsonNode elements = elementsRow.get("elements");

        for (int i = 0; i < unites.size() && i < elements.size(); i++) {
            JsonNode element = elements.get(i);
            UniteSoins unitee = unites.get(i);

            // Déterminer si l'élément contient des données valides
            JsonNode statusNode = element.get("status");
            boolean valide = statusNode != null && statusNode.asText().equals("OK");

            // Tenter de récupérer les objets principaux (Distance et Durée)
            JsonNode distanceObject = element.get("distance");
            JsonNode durationObject = element.get("duration_in_traffic");

            // Extraction sécurisée de la distance
            long distanceMetres = -1;
            // La vérification .has("value") est essentielle pour ne pas planter sur un
            // MissingNode
            if (valide && distanceObject != null && distanceObject.has("value")) {
                distanceMetres = distanceObject.get("value").asLong();
            }

            // Extraction sécurisée de la durée
            long dureeSecondes = -1;
            if (valide && durationObject != null && durationObject.has("value")) {
                dureeSecondes = durationObject.get("value").asLong();
            }

            // Création de la Destination Calculee
            DestinationCalculeeDTO dc = new DestinationCalculeeDTO(
                    new PositionGPS(unitee.getLatitude().doubleValue(), unitee.getLongitude().doubleValue()),
                    unitee.getAdresse(),
                    distanceMetres,
                    dureeSecondes,
                    valide);

            // Création du DTO final
            resultats.add(new UniteeSoinsTrajetDTO(unitee, dc));
        }

        return resultats;
    }

}
