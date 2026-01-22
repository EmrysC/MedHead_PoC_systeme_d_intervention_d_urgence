package MeadHead.Poc.entites;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import MeadHead.Poc.enums.TypeDeRole;

class UserTest {

    @Test
    @DisplayName("UserDetails : getUsername doit retourner l'email de l'utilisateur")
    void getUsername_ShouldReturnEmail() {
        // Given
        String email = "utilisateur1@compte.com";
        User user = User.builder()
                .email(email)
                .nom("Mead")
                .prenom("Head")
                .build();

        // When & Then
        // On vérifie que getUsername() retourne bien l'email et non plus "Mead Head"
        assertThat(user.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("UserDetails : getAuthorities doit retourner une liste vide si le rôle est nul")
    void getAuthorities_ShouldReturnEmptyListWhenRoleIsNull() {
        User user = new User();
        user.setRole(null); // Branche : if (this.role == null)

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("UserDetails : getAuthorities doit mapper le rôle correctement")
    void getAuthorities_ShouldMapRoleToAuthority() {
        User user = User.builder().role(TypeDeRole.ROLE_USER).build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("UserDetails : Les méthodes de statut doivent retourner la valeur de 'active'")
    void testAccountStatusMethods() {
        // Test quand le compte est actif
        User activeUser = User.builder().active(true).build();
        assertThat(activeUser.isAccountNonExpired()).isTrue();
        assertThat(activeUser.isAccountNonLocked()).isTrue();
        assertThat(activeUser.isCredentialsNonExpired()).isTrue();
        assertThat(activeUser.isEnabled()).isTrue();

        // Test quand le compte est inactif
        User inactiveUser = User.builder().active(false).build();
        assertThat(inactiveUser.isAccountNonExpired()).isFalse();
        assertThat(inactiveUser.isAccountNonLocked()).isFalse();
        assertThat(inactiveUser.isCredentialsNonExpired()).isFalse();
        assertThat(inactiveUser.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Lombok : Test du AllArgsConstructor")
    void testAllArgsConstructor() {
        User user = new User(2L, "mail@test.com", "pass", false, TypeDeRole.ROLE_USER, "Nom", "Prenom");
        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.isActive()).isFalse();
    }
}
