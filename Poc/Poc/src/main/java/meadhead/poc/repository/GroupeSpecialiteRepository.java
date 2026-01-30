package meadhead.poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import meadhead.poc.entites.GroupeSpecialite;

@Repository
public interface GroupeSpecialiteRepository extends JpaRepository<GroupeSpecialite, Long> {

}
