package meadhead.poc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import meadhead.poc.entites.UniteSoins;

public interface UniteSoinsRepository extends JpaRepository<UniteSoins, Long> {

    List<UniteSoins> findBySpecialisationIdAndLitsDisponiblesGreaterThan(Long specialisationId, int litsDisponibles);

    List<UniteSoins> findBySpecialisationNomAndLitsDisponiblesGreaterThan(String nomSpecialisation, int litsDisponibles);

}
