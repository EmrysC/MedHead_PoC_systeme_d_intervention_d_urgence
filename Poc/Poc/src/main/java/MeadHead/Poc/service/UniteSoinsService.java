package MeadHead.Poc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;


@Service
public class UniteSoinsService {


    private UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsService(@Autowired UniteSoinsRepository uniteSoinsRepository){
        this.uniteSoinsRepository = uniteSoinsRepository;  
    }

     public void creer(UniteSoins uniteSoins){
        this.uniteSoinsRepository.save(uniteSoins);
    }

    public UniteSoins trouverParId(Long id){
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }

    public List<UniteSoins> listerTous(){
        return this.uniteSoinsRepository.findAll();
    }

    public List<UniteSoins> rechercherLitDisponible(String nomSpecialisation) { 
    return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
        nomSpecialisation, 
        0 
    );


    private String distance getDistance(
        @RequestParam double lat1, @RequestParam double lon1,
        @RequestParam double lat2, @RequestParam double lon2) {

        double distanceMeters = googleMapsClient.calculerDistance(lat1, lon1, lat2, lon2);


        return distanceMeters / 1000; // Convertir en kilomètres

    }
}

}
