package meadhead.poc.service;

import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import meadhead.poc.entites.Reservation;
import meadhead.poc.entites.UniteSoins;
import meadhead.poc.entites.User;
import meadhead.poc.exception.exeption_list.LitIndisponibleException;
import meadhead.poc.exception.exeption_list.UniteSoinsNotFoundException;
import meadhead.poc.repository.ReservationRepository;
import meadhead.poc.repository.UniteSoinsRepository;
import meadhead.poc.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final ReservationRepository reservationRepository;
    // 1. Ajoutez le repository des utilisateurs
    private final UserRepository userRepository;

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, JpaSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void reserverLit(@NonNull Long uniteSoinsId, @NonNull User user) {

        // 2. Rechargez l'utilisateur depuis la base pour avoir un objet "géré" (managed) complet
        // Cela évite les erreurs de contraintes NULL sur le Nom/Prénom
        User managedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + user.getEmail()));

        // Chercher l'unité
        UniteSoins uniteSoins = uniteSoinsRepository.findById(uniteSoinsId)
                .orElseThrow(() -> new UniteSoinsNotFoundException(uniteSoinsId));

        // Vérifier la disponibilité (protection contre les valeurs NULL en base)
        Integer currentLits = uniteSoins.getLitsDisponibles();
        if (currentLits == null || currentLits <= 0) {
            throw new LitIndisponibleException(uniteSoinsId);
        }

        // Décrémentation
        uniteSoins.setLitsDisponibles(currentLits - 1);
        uniteSoinsRepository.save(uniteSoins);

        // 3. Utilisez 'managedUser' (l'objet complet) pour la réservation
        Reservation nouvelleReservation = new Reservation(uniteSoins, managedUser);

        // 4. Utilisez saveAndFlush pour forcer la validation MariaDB immédiatement
        reservationRepository.saveAndFlush(nouvelleReservation);
    }
}
