package meadhead.poc.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import meadhead.poc.dto.SpecialisationGroupeDTO;
import meadhead.poc.service.GroupeSpecialiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("specilites")
public class GroupeSpecialiteController {

    private final GroupeSpecialiteService groupeSpecialiteService;

    public GroupeSpecialiteController(GroupeSpecialiteService groupeSpecialiteService) {
        this.groupeSpecialiteService = groupeSpecialiteService;
    }

    // @formatter:off
    @Operation(
            summary = "[PROD] Liste groupe spécialités et spécialités",
            description = "Liste groupe spécialités et spécialités",
            responses = {
                // 200 SUCESS (Retourne les groupes de spécialités et les spécalités )
                @ApiResponse(responseCode = "200", description = "Liste des groupes de spécialités retournée avec succès.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[{\"id\":1,\"nom\":\"Cardiologie\",\"specialisations\":[{\"id\":\"5\",\"nom\":\"Cardiologie Consultation\"},{\"id\":\"1\",\"nom\":\"Cardiologie Interventionnelle\"},{\"id\":\"6\",\"nom\":\"Cardiologie Rythmologie\"},{\"id\":\"7\",\"nom\":\"Cardiologie Électrophysiologie\"}]},{\"id\":2,\"nom\":\"Neurologie\",\"specialisations\":[{\"id\":\"2\",\"nom\":\"Neurologie Pédiatrique\"}]},{\"id\":4,\"nom\":\"Orthopédie\",\"specialisations\":[{\"id\":\"4\",\"nom\":\"Orthopédie Traumatologique\"}]},{\"id\":3,\"nom\":\"Pédiatrie\",\"specialisations\":[{\"id\":\"3\",\"nom\":\"Pédiatrie Générale\"}]}]}")))})
    // @formatter:on
    @GetMapping
    public List<SpecialisationGroupeDTO> getSpecialitesGroupes() {
        return groupeSpecialiteService.getAllSpecialiteGroupesAsDTO();
    }
}
