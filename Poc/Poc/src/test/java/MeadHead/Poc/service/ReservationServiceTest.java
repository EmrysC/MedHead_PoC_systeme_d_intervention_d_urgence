package MeadHead.Poc.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import MeadHead.Poc.entites.Reservation;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;
import MeadHead.Poc.repository.ReservationRepository;
import MeadHead.Poc.repository.UniteSoinsRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
class ReservationServiceTest {

    @Mock
    private UniteSoinsRepository uniteSoinsRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("reserverLit : Succès - Décrémente le lit et crée la réservation")
    void reserverLit_Success() {
        // Given
        Long uniteId = 1L;
        User user = new User();
        user.setNom("Jean");

        UniteSoins unite = new UniteSoins();
        unite.setId(uniteId);
        unite.setLitsDisponibles(5);

        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.of(unite));

        // When
        reservationService.reserverLit(uniteId, user);

        // Then
        // Vérifie la décrémentation
        assertThat(unite.getLitsDisponibles()).isEqualTo(4);

        //  Vérifie les sauvegardes
        verify(uniteSoinsRepository, times(1)).save(unite);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(reservationCaptor.capture());

        Reservation createdReservation = reservationCaptor.getValue();
        assertThat(createdReservation.getUser()).isEqualTo(user);
        assertThat(createdReservation.getUniteSoins()).isEqualTo(unite);
    }

    @Test
    @DisplayName("reserverLit : Échec - Unité de soins introuvable (Exception 404)")
    void reserverLit_UniteNotFound() {
        // Given
        Long uniteId = 1L;
        User user = new User();
        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UniteSoinsNotFoundException.class, ()
                -> reservationService.reserverLit(uniteId, user)
        );

        verify(uniteSoinsRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserverLit : Échec - Aucun lit disponible (Exception 400)")
    void reserverLit_NoBedsAvailable() {
        // Given
        Long uniteId = 1L;
        User user = new User();
        UniteSoins unite = new UniteSoins();
        unite.setLitsDisponibles(0); // Branche : if (uniteSoins.getLitsDisponibles() <= 0)

        when(uniteSoinsRepository.findById(uniteId)).thenReturn(Optional.of(unite));

        // When & Then
        assertThrows(LitIndisponibleException.class, ()
                -> reservationService.reserverLit(uniteId, user)
        );

        verify(uniteSoinsRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }
}
