package MeadHead.Poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import MeadHead.Poc.entites.Specialisation;

@Repository
public interface SpecialisationRepository extends JpaRepository<Specialisation, Long> {

}