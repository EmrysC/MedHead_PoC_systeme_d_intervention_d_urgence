package MeadHead.Poc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeadHead.Poc.entites.Reservation;
import MeadHead.Poc.entites.UniteSoins;
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
    public void reserverLit(Long uniteSoinsId, String utilisateurEmail) {

        // Chercher l'unité de soins
        UniteSoins uniteSoins = uniteSoinsRepository.findById(uniteSoinsId).orElseThrow(() -> new UniteSoinsNotFoundException(uniteSoinsId));

        // Vérifier la disponibilité
        if (uniteSoins.getLitsDisponibles() <= 0) {
            throw new LitIndisponibleException(uniteSoinsId);
        }

        uniteSoinsRepository.save(uniteSoins);
        // Effectuer la réservation (Décrémenter)
        uniteSoins.setLitsDisponibles(uniteSoins.getLitsDisponibles() - 1);

        // Enregistrer la réservation
        Reservation nouvelleReservation = new Reservation(uniteSoinsId, utilisateurEmail);
        reservationRepository.save(nouvelleReservation);

    }
}
