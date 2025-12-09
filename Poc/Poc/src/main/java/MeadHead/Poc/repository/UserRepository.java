package MeadHead.Poc.repository;

import org.springframework.data.repository.CrudRepository;

import MeadHead.Poc.entites.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {


    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
