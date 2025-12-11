package MeadHead.Poc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import MeadHead.Poc.dto.ReservationRequestDTO;
import MeadHead.Poc.service.ReservationService;

@RestController
@RequestMapping(path = "reservation")
@RequiredArgsConstructor // Pour l'injection du ReservationService via constructeur
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Endpoint sécurisé pour l'action de réserver un lit.
     * Requête : POST /api/reservation/lit
     */
    @PostMapping(value = "/lit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> reserverLit(
            @RequestBody ReservationRequestDTO dto) {

        // Récupérer l'identité de l'utilisateur connecté via Spring Security (JWT)
        String utilisateurEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Appel au service pour effectuer la logique de réservation 
        reservationService.reserverLit(
                dto.getUniteSoinsId(),
                utilisateurEmail);

        return ResponseEntity.noContent().build();
    }
}