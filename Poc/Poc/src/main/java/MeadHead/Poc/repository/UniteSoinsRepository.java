package MeadHead.Poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import MeadHead.Poc.entites.UniteSoins;

import java.util.List;

public interface UniteSoinsRepository extends JpaRepository<UniteSoins, Long> {
    
    List<UniteSoins> findBySpecialisationNomAndLitsDisponiblesGreaterThan(String nomSpecialisation, int litsDisponibles);

}
