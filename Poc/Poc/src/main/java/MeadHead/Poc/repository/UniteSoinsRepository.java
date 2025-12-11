package MeadHead.Poc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import  MeadHead.Poc.entites.UniteSoins;


public interface UniteSoinsRepository extends JpaRepository<UniteSoins, Long> {
    
    List<UniteSoins> findBySpecialisationNomAndLitsDisponiblesGreaterThan(String nomSpecialisation, int litsDisponibles);

}
