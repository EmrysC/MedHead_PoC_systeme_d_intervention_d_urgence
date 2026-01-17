package MeadHead.Poc.service;

import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeadHead.Poc.entites.Reservation;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.entites.User;
import MeadHead.Poc.exception.exeption_list.LitIndisponibleException;
import MeadHead.Poc.exception.exeption_list.UniteSoinsNotFoundException;
import MeadHead.Poc.repository.ReservationRepository;
import MeadHead.Poc.repository.UniteSoinsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, JpaSystemException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void reserverLit(@NonNull Long uniteSoinsId, @NonNull User user) {

//  Chercher l'unité (on récupère l'objet complet)
        UniteSoins uniteSoins = uniteSoinsRepository.findById(uniteSoinsId)
                .orElseThrow(() -> new UniteSoinsNotFoundException(uniteSoinsId));

        // Vérifier la disponibilité
        if (uniteSoins.getLitsDisponibles() <= 0) {
            throw new LitIndisponibleException(uniteSoinsId);
        }

        // Modifier la valeur (Décrémentation)
        uniteSoins.setLitsDisponibles(uniteSoins.getLitsDisponibles() - 1);

        // Enregistrer les modifications de l'unité
        uniteSoinsRepository.save(uniteSoins);

        //  Créer la réservation 
        Reservation nouvelleReservation = new Reservation(uniteSoins, user);

        // Enregistrer la réservation
        reservationRepository.save(nouvelleReservation);

    }
}
