package MeadHead.Poc.repository;
import  MeadHead.Poc.entites.UniteSoins;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniteSoinsRepository extends JpaRepository<UniteSoins, Long> {
    
    List<UniteSoins> findBySpecialisationNomAndLitsDisponiblesGreaterThan(String nomSpecialisation, int litsDisponibles);

}
