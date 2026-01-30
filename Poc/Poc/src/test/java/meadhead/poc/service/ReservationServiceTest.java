package meadhead.poc.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import meadhead.poc.entites.Reservation;
import meadhead.poc.entites.UniteSoins;
import meadhead.poc.entites.User;
import meadhead.poc.exception.exeption_list.LitIndisponibleException;
import meadhead.poc.exception.exeption_list.UniteSoinsNotFoundException;
import meadhead.poc.repository.ReservationRepository;
import meadhead.poc.repository.UniteSoinsRepository;
import meadhead.poc.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
@SuppressWarnings({"null"})
class ReservationServiceTest {

    @Mock
    private UniteSoinsRepository uniteSoinsRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("reserverLit : Succès - Décrémente le lit et crée la réservation")
    void reserverLit_Success() {
        // Given
        Long uniteId = 1L;
        String testEmail = "utilisateur1@compte.com";

        User user = new User();
        user.setEmail(testEmail); //  L'email  indispensable 
        user.setNom("Jean");

        UniteSoins unite = new UniteSoins();
        unite.setId(uniteId);
        unite.setLitsDisponibles(5);
        unite.setVersion(0L);

        // 3. simule que la base de données trouve bien l'utilisateur par son mail
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.of(unite));

        // When
        reservationService.reserverLit(uniteId, user);

        // Then
        assertThat(unite.getLitsDisponibles()).isEqualTo(4);
        verify(uniteSoinsRepository, times(1)).save(unite);

        // On vérifie que le repository de réservation a bien utilisé saveAndFlush
        verify(reservationRepository, times(1)).saveAndFlush(any(Reservation.class));

        // On vérifie que findByEmail a été appelé une fois
        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("reserverLit : Échec - Unité de soins introuvable (Exception 404)")
    void reserverLit_UniteNotFound() {
        // Given
        Long uniteId = 1L;
        User user = new User();
        user.setEmail("test@exemple.com");

        // On simule que l'utilisateur est trouvé par son mail
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // On simule que l'unité n'est pas trouvée
        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.empty());

        // When & Then
        UniteSoinsNotFoundException exception = assertThrows(UniteSoinsNotFoundException.class, ()
                -> reservationService.reserverLit(uniteId, user)
        );

        assertThat(exception.getMessage()).isNotBlank();
        verify(uniteSoinsRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserverLit : Échec - Aucun lit disponible (Exception 400)")
    void reserverLit_NoBedsAvailable() {
        // Given
        Long uniteId = 1L;
        User user = new User();
        user.setEmail("test@exemple.com");

        UniteSoins unite = new UniteSoins();
        unite.setLitsDisponibles(0);

        // On simule que l'utilisateur est trouvé
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // On simule que l'unité est trouvée
        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.of(unite));

        // When & Then
        LitIndisponibleException exception = assertThrows(LitIndisponibleException.class, ()
                -> reservationService.reserverLit(uniteId, user)
        );

        assertThat(exception.getMessage()).isNotBlank();
        verify(uniteSoinsRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }
}
