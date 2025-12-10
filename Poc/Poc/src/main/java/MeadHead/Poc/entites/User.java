/* https://www.youtube.com/watch?v=awP1N0R9rx0&t=10s */

package MeadHead.Poc.entites;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "active", nullable = false)
    private boolean active = true; // Compte actif par défaut

    @Column(name = "roles", nullable = false)
    private String role; // Rôles de l'utilisateur (ex: ROLE_USER, ROLE_ADMIN)

    @Column(name = "Nom", nullable = false)
    private String nom;

    @Column(name = "Prenom", nullable = false)
    private String prenom;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = this.role; // Utilise le champ 'role' de l'entité

        // Gérer les cas où le rôle est nul ou vide
        if (roleName == null || roleName.trim().isEmpty()) {
            return List.of();
        }

        // Nettoyage si le rôle est encodé par le JWT (ex: [ROLE_USER])
        if (roleName.startsWith("[") && roleName.endsWith("]")) {
            roleName = roleName.substring(1, roleName.length() - 1).replace("\"", "").trim();
        }

        String finalRole = roleName.split(",")[0].trim();

        if (finalRole.isEmpty()) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority(finalRole));

    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.nom + " " + this.prenom;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.active;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

}
