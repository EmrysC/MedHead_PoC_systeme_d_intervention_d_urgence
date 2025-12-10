package MeadHead.Poc.service;

import MeadHead.Poc.repository.UserRepository;
import MeadHead.Poc.dto.UserCreationDTO;
import MeadHead.Poc.entites.User;

import lombok.AllArgsConstructor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;


@AllArgsConstructor
@Service
public class UserService implements UserDetailsService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public User createUser(UserCreationDTO userDto) {

        this.validateEmail(userDto.getEmail());

        // Mappage DTO -> Entité
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setRole("ROLE_USER");
        user.setActive(true);
        String mdpCrypt = this.bCryptPasswordEncoder.encode(userDto.getPassword());
        user.setPassword(mdpCrypt);

        return userRepository.save(user);
    }

  @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("--- SERVICE TRACE: TENTATIVE DE CHARGEMENT DE L'UTILISATEUR ---");
        System.out.println("--- SERVICE TRACE: EMAIL REÇU DU JWT: " + email);
        
        // On utilise l'email comme identifiant unique
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Si l'utilisateur n'est pas trouvé, on lance l'exception standard
        if (userOptional.isEmpty()) {
            System.out.println("--- SERVICE TRACE: ÉCHEC: Utilisateur non trouvé en base de données.");
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'e-mail : " + email);
        }

        System.out.println("--- SERVICE TRACE: SUCCÈS: Utilisateur trouvé et chargé." );
        // On retourne l'entité User (qui implémente UserDetails)
        return userOptional.get(); 
    }

    private void validateEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide.");
        }

        Matcher matcher = EMAIL_PATTERN.matcher(email);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Le format de l'adresse e-mail est invalide : " + email);
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée par un autre compte." + email);
        }

    }

}
