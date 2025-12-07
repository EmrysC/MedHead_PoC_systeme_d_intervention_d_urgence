package MeadHead.Poc.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
                JsonNode.class
            );
            
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

}
