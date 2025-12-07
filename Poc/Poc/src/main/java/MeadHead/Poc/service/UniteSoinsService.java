package MeadHead.Poc.service;

import java.util.List;
import org.springframework.stereotype.Service;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;

@Service
public class UniteSoinsService {

    private final UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsService(UniteSoinsRepository uniteSoinsRepository) {
        this.uniteSoinsRepository = uniteSoinsRepository;
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
}