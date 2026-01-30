package meadhead.poc.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import meadhead.poc.dto.PositionDTO;
import meadhead.poc.dto.SpecialisationDetailDTO;
import meadhead.poc.dto.SpecialisationTrajetDTO;
import meadhead.poc.dto.TrajetReponseDTO;
import meadhead.poc.dto.TrajetResultatDTO;
import meadhead.poc.entites.UniteSoins;
import meadhead.poc.exception.exeption_list.ExternalServiceFailureException;
import meadhead.poc.exception.exeption_list.NoBedAvailableException;
import meadhead.poc.gestion_position_trajet.GoogleMapsClient;
import meadhead.poc.gestion_position_trajet.UniteeSoinsTrajetDTO;
import meadhead.poc.repository.UniteSoinsRepository;

@Service
@RequiredArgsConstructor
public class UniteSoinsService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final GoogleMapsClient googleMapsClient;
    private final SpecialisationService specialisationService;

    @Value("${google.api.limit-destinations}")
    private int limitDestinations;

    @Transactional
    public void creer(@NonNull UniteSoins uniteSoins) {
        this.uniteSoinsRepository.save(uniteSoins);
    }

    public UniteSoins trouverParId(@NonNull Long id) {
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
            if (positionDTO.onlyAdresseIsValid()) {
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
                    .toList();
        } else {
            topNProches = uniteSoinsDisponibles;
        }
        // Calculer les trajets bruts
        TrajetResultatDTO trajetResultatAPI = googleMapsClient.calculeerTrajetsOptimises(positionDTO, topNProches);

        // Filtrer : Conserver uniquement les trajets qui sont valides
        List<UniteeSoinsTrajetDTO> uniteSoinsDisponiblesTrajetsValides = trajetResultatAPI.getUnitesSoinsTrajets()
                .stream()
                .filter(trajet -> trajet.getDestinationCalculee().isTrajetValide())
                .toList();

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

    public TrajetReponseDTO calculerTrajetReponse(@NonNull SpecialisationTrajetDTO specialisationTrajetDTO) {

        // Construite SpecialisationDetailDTO
        Assert.notNull(specialisationTrajetDTO.getSpecialisationId(), "Le specialisationId est requis");
        SpecialisationDetailDTO details = specialisationService
                .getSpecialisationDetailsDTO(Objects.requireNonNull(specialisationTrajetDTO.getSpecialisationId()));

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
