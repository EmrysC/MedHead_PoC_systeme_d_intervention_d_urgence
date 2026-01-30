package meadhead.poc.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import meadhead.poc.entites.User;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
