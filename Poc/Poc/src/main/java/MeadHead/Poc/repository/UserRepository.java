package MeadHead.Poc.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import MeadHead.Poc.entites.User;


public interface UserRepository extends CrudRepository<User, Long> {


    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
