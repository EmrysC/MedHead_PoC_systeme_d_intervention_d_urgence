package MeadHead.Poc.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Préparer l'objet PositionDTO de l'origine pour l'API
        PositionDTO positionDTO = new PositionDTO(specialisationTrajetDTO);

        // Calculer les trajets bruts
        TrajetResultatDTO trajetResultatAPI = TrajetResultatDTO.builder().build();
        trajetResultatAPI = googleMapsClient.calculeerTrajetsOptimises(
                positionDTO,
                uniteSoinsDisponibles);

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

}
