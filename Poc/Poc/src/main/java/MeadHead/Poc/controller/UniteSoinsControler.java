package MeadHead.Poc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import MeadHead.Poc.dto.SpecialisationTrajetDTO;
import MeadHead.Poc.dto.TrajetReponseDTO;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;
import MeadHead.Poc.service.UniteSoinsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "unitesoins")
public class UniteSoinsControler {

    private final UniteSoinsService uniteSoinsService;

    private final UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsControler(UniteSoinsRepository uniteSoinsRepository, UniteSoinsService uniteSoinsService) {
        this.uniteSoinsRepository = uniteSoinsRepository;
        this.uniteSoinsService = uniteSoinsService;
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void creer(@RequestBody UniteSoins uniteSoins) {
        uniteSoinsRepository.save(uniteSoins);
    }

    /**
     * Endpoint qui renvoie la liste de toutes les unités de soins. Exemple: GET
     * localhost:8080/api/unitesoins
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    /**
     * Endpoint qui renvoie les détails d'une unité de soins par son ID.
     * Exemple: GET localhost:8080/api/unitesoins/2
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UniteSoins trouverParId(@PathVariable Long id) {
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }

    /**
     * Endpoint qui renvoie les unités de soins avec des lits disponibles pour
     * une spécialisation donnée. Exemple: GET
     * http://localhost:8080/api/unitesoins/recherche_lit_dispo?specialisation=Cardiologie%20Interventionnelle
     */
    @GetMapping(path = "/recherche_lit_dispo", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> rechercherLitDisponible(@RequestParam("specialisation") String nomSpecialisation) {
        return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
                nomSpecialisation,
                0);
    }

    /**
     * Endpoint qui renvoie la liste enrichie des trajets optimisés vers les
     * unités de soins disposant de lits pour une spécialisation donnée, à
     * partir d'une position GPS. Exemple: GET
     * http://localhost:8080/api/trajets_optimises?lat=45.18719605665046&lon=5.68936461721841&specialisation=Cardiologie%20Interventionnelle
     */
    /* 
    @GetMapping("/trajets_optimises")
    public List<UniteeSoinsTrajetDTO> obtenirTrajetsOptimises(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("specialisation") String specialisationNom) {

        return this.uniteSoinsService.calculerTrajetsOptimises(specialisationNom, lat, lon);
    }*/
    // @formatter:off
    @Operation(
            summary = "Ajouter une position GPS",
            description = "Associe une nouvelle position GPS à une unité de soins existante via son ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Coordonnées GPS à associer.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpecialisationTrajetDTO.class))),
            responses = {
                // 200 SUCCESS
                @ApiResponse(responseCode = "200", description = "Retourne les trajet en fonction des unité de soins disponibles"),

                // 400 BAD REQUEST (Erreur de validation du DTO)
                @ApiResponse(responseCode = "400", description = "Erreur de validation des champs (DTO non conforme).", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),

                // 404 NOT FOUND (Aucune unité de soins ne correspond à cet ID)
                @ApiResponse(responseCode = "404", description = "Unité de soins non trouvée.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),

                // 409 CONFLICT (Lits indisponibles)
                @ApiResponse(responseCode = "409", description = "Conflit : Unités de soins est trouvées, mais aucun lit n'est disponible.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),

                // 503 SERVICE UNAVAILABLE (API Google en échec)
                @ApiResponse(responseCode = "503", description = "Service de calcul de trajet indisponible ou aucune destination n'a pu être calculée.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),})
    // @formatter:on
    @GetMapping(path = "/trajets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TrajetReponseDTO ajouterPositionGps(
            @Valid @RequestBody SpecialisationTrajetDTO specialisationTrajetDTO) {

        return this.uniteSoinsService.calculerTrajetReponse(specialisationTrajetDTO);

    }
}
