package MeadHead.Poc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import MeadHead.Poc.entites.Reservation;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.ReservationRepository;
import MeadHead.Poc.repository.UniteSoinsRepository;


@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UniteSoinsRepository uniteSoinsRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserverLit(Long uniteSoinsId, String utilisateurEmail) {

        // Chercher l'unité de soins
        UniteSoins uniteSoins = uniteSoinsRepository.findById(uniteSoinsId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Unité de soins avec l'ID " + uniteSoinsId + " introuvable." 
                ));
        
        // Vérifier la disponibilité
        if (uniteSoins.getLitsDisponibles() <= 0) {
            throw new IllegalStateException(
                "Plus de lits disponibles dans l'unité " + uniteSoinsId 
            );
        }
        
        uniteSoinsRepository.save(uniteSoins);
        // Effectuer la réservation (Décrémenter)
        uniteSoins.setLitsDisponibles(uniteSoins.getLitsDisponibles() - 1);

        // Enregistrer la réservation
        Reservation nouvelleReservation = new Reservation(uniteSoinsId, utilisateurEmail);
        reservationRepository.save(nouvelleReservation);
        
    }
}