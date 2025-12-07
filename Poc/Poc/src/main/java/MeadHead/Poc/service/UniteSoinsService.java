package MeadHead.Poc.service;

import java.util.List;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import MeadHead.Poc.GoogleMapsClient;
import MeadHead.Poc.PositionGPS;
import MeadHead.Poc.UniteeSoinsTrajet;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;

@Service
public class UniteSoinsService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final GoogleMapsClient googleMapsClient;

    public UniteSoinsService(UniteSoinsRepository uniteSoinsRepository, GoogleMapsClient googleMapsClient) {
        this.uniteSoinsRepository = uniteSoinsRepository;
        this.googleMapsClient = googleMapsClient;
    }


    public void creer(UniteSoins uniteSoins) {
        this.uniteSoinsRepository.save(uniteSoins);
    }

    public UniteSoins trouverParId(Long id) {
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }


    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    public List<UniteSoins> rechercherLitDisponible(String nomSpecialisation) {
        // La valeur '0' passée comme deuxième argument assure que LitsDisponibles > 0 est vérifié.
        return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
            nomSpecialisation, 
            0 
        );
    }


    public List<UniteeSoinsTrajet> calculerTrajetsOptimises(String specialisationNom, double origineLat, double origineLon) {
        

        List<UniteSoins> unitesDisponibles = this.rechercherLitDisponible(specialisationNom);
        if (unitesDisponibles.isEmpty()) {
            return List.of(); 
        }

        PositionGPS PositionGpsOrigine = new PositionGPS(origineLat, origineLon);

       List<UniteeSoinsTrajet> trajetsCalcules =  googleMapsClient.calculeerTrajetsOptimises(PositionGpsOrigine, unitesDisponibles);

       if (trajetsCalcules.isEmpty()) {
        return List.of(); 
        }

    trajetsCalcules.sort(
        Comparator.comparingLong(trajet -> trajet.getDestinationCalculee().getDistanceMetres())
    );

    return trajetsCalcules;
  
}
}