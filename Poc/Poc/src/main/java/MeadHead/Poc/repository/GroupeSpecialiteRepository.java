package MeadHead.Poc.repository;

import MeadHead.Poc.entites.GroupeSpecialite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupeSpecialiteRepository extends JpaRepository<GroupeSpecialite, Long> {
    
}