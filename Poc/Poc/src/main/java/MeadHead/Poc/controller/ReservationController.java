package MeadHead.Poc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MeadHead.Poc.dto.ReservationRequestDTO;
import MeadHead.Poc.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "reservation")
@RequiredArgsConstructor // Pour l'injection du ReservationService via constructeur
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Endpoint sécurisé pour l'action de réserver un lit. Requête : POST
     * /api/reservation/lit
     */
    // @formatter:off
    @Operation(
            summary = "[PROD] Réservation d'un lit",
            description = "Liste groupe spécialités et spécialités",
            responses = {
                // 200 SUCESS (Retourne les groupes de spécialités et les spécalités )
                @ApiResponse(responseCode = "200", description = "[PROD] Liste des groupes de spécialités retournée avec succès.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[{\"id\":1,\"nom\":\"Cardiologie\",\"specialisations\":[{\"id\":\"5\",\"nom\":\"Cardiologie Consultation\"},{\"id\":\"1\",\"nom\":\"Cardiologie Interventionnelle\"},{\"id\":\"6\",\"nom\":\"Cardiologie Rythmologie\"},{\"id\":\"7\",\"nom\":\"Cardiologie Électrophysiologie\"}]},{\"id\":2,\"nom\":\"Neurologie\",\"specialisations\":[{\"id\":\"2\",\"nom\":\"Neurologie Pédiatrique\"}]},{\"id\":4,\"nom\":\"Orthopédie\",\"specialisations\":[{\"id\":\"4\",\"nom\":\"Orthopédie Traumatologique\"}]},{\"id\":3,\"nom\":\"Pédiatrie\",\"specialisations\":[{\"id\":\"3\",\"nom\":\"Pédiatrie Générale\"}]}]}")))})
    // @formatter:on
    @PostMapping(value = "/lit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> reserverLit(
            @Valid @RequestBody ReservationRequestDTO dto) {

        // Récupérer l'identité de l'utilisateur connecté via Spring Security (JWT)
        String utilisateurEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Appel au service pour effectuer la logique de réservation 
        reservationService.reserverLit(
                dto.getUniteSoinsId(),
                utilisateurEmail);

        return ResponseEntity.noContent().build();
    }
}
