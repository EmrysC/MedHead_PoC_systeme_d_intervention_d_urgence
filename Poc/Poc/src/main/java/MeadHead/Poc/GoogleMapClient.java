package MeadHead.Poc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

//https://developers.google.com/maps/documentation/distance-matrix/overview?hl=fr


public class GoogleMapClient {


    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.api.url}")
    private String googleApiUrl;



    public String buildDistanceUrl(double lat1, double lon1, double lat2, double lon2) {
        
        final String origins = lat1 + "," + lon1;
        final String destinations = lat2 + "," + lon2;

        String url = UriComponentsBuilder.fromUriString(googleApiUrl) 
                .queryParam("origins", origins)
                .queryParam("destinations", destinations)
                .queryParam("units", "metric")
                .queryParam("key", googleApiKey)
                .toUriString();

        return url;
    }

}
