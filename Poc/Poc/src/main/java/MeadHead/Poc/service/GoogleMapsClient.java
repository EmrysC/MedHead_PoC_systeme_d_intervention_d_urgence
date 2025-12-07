package MeadHead.Poc.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import MeadHead.Poc.DestinationCalculee;
import MeadHead.Poc.PositionGPS;
import MeadHead.Poc.UniteeSoinsTrajet;
import MeadHead.Poc.entites.UniteSoins;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;

//https://developers.google.com/maps/documentation/distance-matrix/overview?hl=fr
//https://developers.google.com/maps/documentation/distance-matrix/distance-matrix?hl=fr#maps_http_distancematrix_latlng-sh

@Service
public class GoogleMapsClient {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.api.url}")
    private String googleApiUrl;

    public GoogleMapsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<UniteeSoinsTrajet> calculeerTrajetsOptimises(PositionGPS origine, List<UniteSoins> unitesDisponibles) {

        String destinations = unitesDisponibles.stream()
                .map(u -> u.getLatitude() + "," + u.getLongitude())
                .collect(Collectors.joining("|"));

        JsonNode response = appelerDistanceMatrix(origine.toString(), destinations);

        if (response == null || response.at("/status").asText().equals("ZERO_RESULTS")) {
            return List.of();
        }

        return traiterReponseGoogle(unitesDisponibles, response);

    }

    public JsonNode appelerDistanceMatrix(String origins, String destinations) {

        String encodedOrigins = URLEncoder.encode(origins, StandardCharsets.UTF_8);
        String encodedDestinations = URLEncoder.encode(destinations, StandardCharsets.UTF_8);

        encodedOrigins = encodedOrigins.replace("%2C", ",");
        encodedDestinations = encodedDestinations.replace("%2C", ",");

        encodedOrigins = encodedOrigins.replace("%7C", "|");
        encodedDestinations = encodedDestinations.replace("%7C", "|");

        String url = googleApiUrl
                + "?origins=" + encodedOrigins
                + "&destinations=" + encodedDestinations
                + "&units=metric"
                + "&departure_time=now"
                + "&traffic_model=optimistic"
                + "&key=" + googleApiKey;

        System.out.println("Appel Google Maps URL: " + url);

        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    JsonNode.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                System.out.println("Google API RESPONSE (JSON): \n" + responseEntity.getBody().toString());
                return responseEntity.getBody();
            } else {
                System.err.println("Erreur HTTP reçue de Google Maps: " + responseEntity.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API Google Maps: " + e.getClass().getName() +
                    " Message: " + e.getMessage() + " URL: " + url);
        }
        return null;
    }

    private List<UniteeSoinsTrajet> traiterReponseGoogle(List<UniteSoins> unites, JsonNode response) {

        List<UniteeSoinsTrajet> resultats = new ArrayList<>();

        JsonNode elementsRow = response.get("rows").get(0);
        JsonNode elements = elementsRow.get("elements");

        for (int i = 0; i < unites.size() && i < elements.size(); i++) {
            JsonNode element = elements.get(i);
            UniteSoins unitee = unites.get(i);

            //Déterminer si l'élément contient des données valides
            JsonNode statusNode = element.get("status");
            boolean valide = statusNode != null && statusNode.asText().equals("OK");

            //Tenter de récupérer les objets principaux (Distance et Durée)
            JsonNode distanceObject = element.get("distance");
            JsonNode durationObject = element.get("duration_in_traffic");

            //Extraction sécurisée de la distance
            long distanceMetres = -1;
            // La vérification .has("value") est essentielle pour ne pas planter sur un
            // MissingNode
            if (valide && distanceObject != null && distanceObject.has("value")) {
                distanceMetres = distanceObject.get("value").asLong();
            }

            //Extraction sécurisée de la durée
            long dureeSecondes = -1;
            if (valide && durationObject != null && durationObject.has("value")) {
                dureeSecondes = durationObject.get("value").asLong();
            }

            //Création de la Destination Calculee
            DestinationCalculee dc = new DestinationCalculee(
                    new PositionGPS(unitee.getLatitude().doubleValue(), unitee.getLongitude().doubleValue()),
                    unitee.getAdresse(),
                    distanceMetres,
                    dureeSecondes,
                    valide);

            // Création du DTO final
            resultats.add(new UniteeSoinsTrajet(unitee, dc));
        }
        return resultats;
    }
}
