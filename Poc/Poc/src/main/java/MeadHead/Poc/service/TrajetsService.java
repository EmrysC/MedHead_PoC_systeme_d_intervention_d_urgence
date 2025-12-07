package MeadHead.Poc.service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import MeadHead.Poc.PositionGPS;
import MeadHead.Poc.DestinationCalculee;
import MeadHead.Poc.UniteeSoinsTrajet;
import MeadHead.Poc.entites.UniteSoins;







@Service
public class TrajetsService {
    
    // Injection du client API et du service de données
    private final GoogleMapsClient googleMapsClient;
    private final UniteSoinsService uniteSoinsService; // Pour récupérer la liste des hôpitaux
    
    // CONSTRUCTEUR
    public TrajetsService(GoogleMapsClient googleMapsClient, UniteSoinsService uniteSoinsService) {
        this.googleMapsClient = googleMapsClient;
        this.uniteSoinsService = uniteSoinsService;
    }

    /**
     * Étape 1 : Récupère les unités de soins disponibles (par spécialisation, > 0 lits)
     * Étape 2 : Lance l'appel Google Distance Matrix
     * Étape 3 : Traite la réponse et mappe les résultats
     */
    public List<UniteeSoinsTrajet> calculerTrajetsOptimises(String specialisationNom, double origineLat, double origineLon) {
        
        // 1. Récupérer les destinations (Unités de Soins)
        List<UniteSoins> unitesDisponibles = uniteSoinsService.rechercherLitDisponible(specialisationNom);
        
        if (unitesDisponibles.isEmpty()) {
            return List.of(); // Rien à calculer
        }

        // 2. Préparer les chaînes d'origine et de destination pour l'API Google
        PositionGPS origine = new PositionGPS(origineLat, origineLon);
        String origin = origine.getLatitude() + "," + origine.getLongitude(); 
        
        String destinations = unitesDisponibles.stream()
            .map(u -> u.getLatitude() + "," + u.getLongitude()) 
            .collect(Collectors.joining("|"));

        JsonNode response = googleMapsClient.appelerDistanceMatrix(origin, destinations);
        

        if (response == null || response.at("/status").asText().equals("ZERO_RESULTS")) {
             return List.of();
        }
        
        return traiterReponseGoogle(unitesDisponibles, response);
    }
    

  private List<UniteeSoinsTrajet> traiterReponseGoogle(List<UniteSoins> unites, JsonNode response) {

    List<UniteeSoinsTrajet> resultats = new ArrayList<>();

    JsonNode elementsRow = response.get("rows").get(0);
    JsonNode elements = elementsRow.get("elements");
    


    for (int i = 0; i < unites.size() && i < elements.size(); i++) {
        JsonNode element = elements.get(i);
        UniteSoins unitee = unites.get(i);

        // -- DÉBUT DE LA LOGIQUE DE CORRECTION --

        // 1. Déterminer si l'élément contient des données valides
        JsonNode statusNode = element.get("status");
        boolean valide = statusNode != null && statusNode.asText().equals("OK");

        // 2. Tenter de récupérer les objets principaux (Distance et Durée)
        JsonNode distanceObject = element.get("distance");
        JsonNode durationObject = element.get("duration_in_traffic");

        // 3. Extraction sécurisée de la distance
        long distanceMetres = -1;
        // La vérification .has("value") est essentielle pour ne pas planter sur un MissingNode
        if (valide && distanceObject != null && distanceObject.has("value")) {
            distanceMetres = distanceObject.get("value").asLong();
        }

        // 4. Extraction sécurisée de la durée
        long dureeSecondes = -1;
        if (valide && durationObject != null && durationObject.has("value")) {
            dureeSecondes = durationObject.get("value").asLong();
        }
        
        // -- FIN DE LA LOGIQUE DE CORRECTION --

        // Création de la Destination Calculee
        DestinationCalculee dc = new DestinationCalculee(
            new PositionGPS(unitee.getLatitude().doubleValue(), unitee.getLongitude().doubleValue()),
            unitee.getAdresse(),
            distanceMetres,
            dureeSecondes,
            valide
        );
        
        // Création du DTO final
        resultats.add(new UniteeSoinsTrajet(unitee, dc));
    }
    return resultats;
}
}