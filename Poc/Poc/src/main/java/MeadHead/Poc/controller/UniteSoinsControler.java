package MeadHead.Poc.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import MeadHead.Poc.exception.exeption_list.ValidationManuelleException;
import MeadHead.Poc.repository.UniteSoinsRepository;
import MeadHead.Poc.service.UniteSoinsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping(path = "unitesoins")
public class UniteSoinsControler {

    private final UniteSoinsService uniteSoinsService;

    private final UniteSoinsRepository uniteSoinsRepository;

    private final Validator validator;

    public UniteSoinsControler(Validator validator, UniteSoinsRepository uniteSoinsRepository, UniteSoinsService uniteSoinsService) {
        this.validator = validator;
        this.uniteSoinsRepository = uniteSoinsRepository;
        this.uniteSoinsService = uniteSoinsService;
    }

    @Profile("dev")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void creer(@RequestBody UniteSoins uniteSoins) {
        uniteSoinsRepository.save(uniteSoins);
    }

    /**
     * Endpoint qui renvoie la liste de toutes les unités de soins. Exemple: GET
     * localhost:8080/api/unitesoins
     */
    @Profile("dev")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    /**
     * Endpoint qui renvoie les détails d'une unité de soins par son ID.
     * Exemple: GET localhost:8080/api/unitesoins/2
     */
    @Profile("dev")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UniteSoins trouverParId(@PathVariable Long id) {
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }

    /**
     * Endpoint qui renvoie les unités de soins avec des lits disponibles pour
     * une spécialisation donnée. Exemple: GET
     * http://localhost:8080/api/unitesoins/recherche_lit_dispo?specialisation=Cardiologie%20Interventionnelle
     */
    @Profile("dev")
    @GetMapping(path = "/recherche_lit_dispo", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> rechercherLitDisponible(@RequestParam("specialisation") String nomSpecialisation) {
        return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
                nomSpecialisation,
                0);
    }

    // @formatter:off
    @Operation(
            summary = "[PROD] Rechercher le trajet",
            description = "Donne les trajets des unitées de soins disponible les plus proches",
            responses = {
                @ApiResponse(responseCode = "200", description = "Retourne les trajet en fonction des unité de soins disponibles"),
                @ApiResponse(responseCode = "400", description = "Erreur de validation des champs (DTO non conforme).", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),
                @ApiResponse(responseCode = "404", description = "Unité de soins non trouvée.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),
                @ApiResponse(responseCode = "409", description = "Conflit : Unités de soins est trouvées, mais aucun lit n'est disponible.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),
                @ApiResponse(responseCode = "503", description = "Service de calcul de trajet indisponible ou aucune destination n'a pu être calculée.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "todo"))),})
    // @formatter:on
    @GetMapping(path = "/trajets", produces = MediaType.APPLICATION_JSON_VALUE)
    public TrajetReponseDTO rechercherTrajet(
            @RequestParam Long specialisationId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String adresse) throws MethodArgumentNotValidException {

        // construit le DTO
        SpecialisationTrajetDTO dto = new SpecialisationTrajetDTO(specialisationId, latitude, longitude, adresse);

        // VALIDE MANUELLEMENT
        // crée un objet "BindingResult" pour stocker les erreurs
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "specialisationTrajetDTO");

        // demande au validator de remplir le bindingResult avec les erreurs des annotations (@OneOfAddressOrGps)
        validator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationManuelleException(bindingResult);
        }

        return this.uniteSoinsService.calculerTrajetReponse(dto);
    }
}
