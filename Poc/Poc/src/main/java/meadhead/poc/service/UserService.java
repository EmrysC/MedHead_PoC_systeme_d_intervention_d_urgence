package meadhead.poc.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import meadhead.poc.dto.UserCreationDTO;
import meadhead.poc.entites.User;
import meadhead.poc.enums.TypeDeRole;
import meadhead.poc.exception.exeption_list.EmailAlreadyExistsException;
import meadhead.poc.exception.exeption_list.EmailNotFoundException;
import meadhead.poc.repository.UserRepository;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public User createUser(UserCreationDTO userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            Map<String, String> errorsMap = Map.of(
                    "email",
                    String.format("L'adresse e-mail '%s' est déjà utilisée par un autre compte.", userDto.getEmail()));
            throw new EmailAlreadyExistsException(errorsMap);
        }

        // Mappage DTO -> Entité
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setRole(TypeDeRole.ROLE_USER);
        user.setActive(true);
        String mdpCrypt = this.bCryptPasswordEncoder.encode(userDto.getPassword());
        user.setPassword(mdpCrypt);

        return userRepository.save(user);
    }

    @Override
    public User loadUserByUsername(String email) throws EmailNotFoundException {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Lancer l'exception standard attendue par Spring Security
            throw new UsernameNotFoundException(
                    String.format("Utilisateur non trouvé avec l'e-mail : %s", email));
        }

        return userOptional.get();
    }

}
