package MeadHead.Poc.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UniteSoinsService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final GoogleMapsClient googleMapsClient;
    private final SpecialisationService specialisationService;

    @Value("${google.api.limit-destinations}")
    private int limitDestinations;

    @Transactional
    public void creer(UniteSoins uniteSoins) {
        this.uniteSoinsRepository.save(uniteSoins);
    }

    public UniteSoins trouverParId(Long id) {
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }

    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    public List<UniteSoins> rechercherLitDisponible(Long idSpecialisation) {
        return this.uniteSoinsRepository.findBySpecialisationIdAndLitsDisponiblesGreaterThan(
                idSpecialisation,
                0);
    }

    private TrajetResultatDTO calculerTrajets(SpecialisationTrajetDTO specialisationTrajetDTO) {

        // Rechercher les lits disponibles
        List<UniteSoins> uniteSoinsDisponibles = this
                .rechercherLitDisponible(specialisationTrajetDTO.getSpecialisationId());

        // Vérification lits disponibles (Gère l'erreur 409 NoBedAvailableException)
        if (uniteSoinsDisponibles.isEmpty()) {
            throw new NoBedAvailableException(
                    Map.of("specialisation",
                            String.format("Aucun lit disponible pour la spécialisation id : %s", specialisationTrajetDTO.getSpecialisationId())));
        }

        // Préparer l'objet PositionDTO 
        PositionDTO positionDTO = new PositionDTO(specialisationTrajetDTO);

        List<UniteSoins> topNProches;
        if (uniteSoinsDisponibles.size() > limitDestinations) {

            // Calculé la position GPS si on a seulemment l adresse
            if (positionDTO.OnlyAdresseIsValid()) {
                googleMapsClient.setPositionWithAdresse(positionDTO);
            }

            double latO = positionDTO.getLatitude();
            double lonO = positionDTO.getLongitude();

            topNProches = uniteSoinsDisponibles.stream()
                    .sorted(Comparator.comparingDouble(u
                            -> calculerDistanceVolOiseau(latO, lonO,
                            u.getLatitude().doubleValue(),
                            u.getLongitude().doubleValue())))
                    .limit(limitDestinations) // On ne garde que les N meilleurs pour Google Maps
                    .collect(Collectors.toList());
        } else {
            topNProches = uniteSoinsDisponibles;
        }
        // Calculer les trajets bruts
        TrajetResultatDTO trajetResultatAPI = TrajetResultatDTO.builder().build();
        trajetResultatAPI = googleMapsClient.calculeerTrajetsOptimises(
                positionDTO,
                topNProches);

        // Filtrer : Conserver uniquement les trajets qui sont valides
        List<UniteeSoinsTrajetDTO> uniteSoinsDisponiblesTrajetsValides = trajetResultatAPI.getUnitesSoinsTrajets()
                .stream()
                .filter(trajet -> trajet.getDestinationCalculee().isTrajetValide())
                .collect(Collectors.toList());

        // Vérifier si la liste des trajets valides est vide (Gestion 503)
        if (uniteSoinsDisponiblesTrajetsValides.isEmpty()) {
            throw new ExternalServiceFailureException(
                    Map.of("trajets",
                            "Impossible de calculer les trajets optimisés : le service externe n'a retourné aucun itinéraire valide (même si des destinations étaient disponibles)."));
        }

        // Tri par distance (ascendante)
        uniteSoinsDisponiblesTrajetsValides.sort(
                Comparator.comparingLong(trajet -> trajet.getDestinationCalculee().getDistanceMetres()));

        // Construction et retour de l'objet de réponse composite
        return TrajetResultatDTO.builder()
                .unitesSoinsTrajets(uniteSoinsDisponiblesTrajetsValides)
                .originePosition(trajetResultatAPI.getOriginePosition())
                .build();
    }
    // Calcule les trajets et enrichit la réponse avec les détails du Groupe et de
    // la Spécialisation.

    public TrajetReponseDTO calculerTrajetReponse(SpecialisationTrajetDTO specialisationTrajetDTO) {

        // Construite SpecialisationDetailDTO
        SpecialisationDetailDTO details = specialisationService
                .getSpecialisationDetailsDTO(specialisationTrajetDTO.getSpecialisationId());

        // Calcul les trajets 
        TrajetResultatDTO trajetsCalcules = this.calculerTrajets(specialisationTrajetDTO);

        // Construction du DTO de réponse final
        return TrajetReponseDTO.builder()
                .specialisationDetailDTO(details)
                .trajetsCalculesValide(trajetsCalcules)
                .build();
    }

    private double calculerDistanceVolOiseau(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // Rayon de la Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

}
