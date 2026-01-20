package MeadHead.Poc.entites;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("Constructeur personnalisé : Doit initialiser la date à 'maintenant'")
    void testCustomConstructor() {
        // Given
        UniteSoins unite = new UniteSoins();
        User user = new User();

        // When : Appel du constructeur spécifique
        Reservation reservation = new Reservation(unite, user);

        // Then
        assertThat(reservation.getUniteSoins()).isEqualTo(unite);
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getDateReservation()).isNotNull();

        // Vérifie que la date est correcte (à moins de 1 seconde près)
        assertThat(reservation.getDateReservation())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

}
