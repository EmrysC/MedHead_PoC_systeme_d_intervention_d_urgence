package meadhead.poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import meadhead.poc.entites.Specialisation;

@Repository
public interface SpecialisationRepository extends JpaRepository<Specialisation, Long> {

}
