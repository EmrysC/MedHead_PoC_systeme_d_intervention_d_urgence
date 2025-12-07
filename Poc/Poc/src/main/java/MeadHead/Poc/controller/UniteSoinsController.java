package MeadHead.Poc.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;

import MeadHead.Poc.UniteeSoinsTrajet;
import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;
import MeadHead.Poc.service.UniteSoinsService;

@RestController
@RequestMapping(path = "unitesoins")
public class UniteSoinsController {


    private final UniteSoinsService uniteSoinsService;

    private UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsController(UniteSoinsRepository uniteSoinsRepository, UniteSoinsService uniteSoinsService) {
        this.uniteSoinsRepository = uniteSoinsRepository;
        this.uniteSoinsService = uniteSoinsService;
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void creer(@RequestBody UniteSoins uniteSoins) {
        uniteSoinsRepository.save(uniteSoins);
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
     * Endpoint qui renvoie la liste de toutes les unités de soins.
     * Exemple: GET localhost:8080/api/unitesoins
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    /**
     * Endpoint qui renvoie les unités de soins avec des lits disponibles pour une
     * spécialisation donnée.
     * Exemple: GET
     * http://localhost:8080/api/unitesoins/recherche_lit_dispo?specialisation=Cardiologie%20Interventionnelle
     */
    @GetMapping(path = "/recherche_lit_dispo", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> rechercherLitDisponible(@RequestParam("specialisation") String nomSpecialisation) {
        return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
                nomSpecialisation,
                0);
    }

    /**
     * Endpoint qui renvoie la liste enrichie des trajets optimisés vers les unités
     * de soins
     * disposant de lits pour une spécialisation donnée, à partir d'une position
     * GPS.
     * Exemple: GET
     * http://localhost:8080/api/trajets_optimises?lat=45.18719605665046&lon=5.68936461721841&specialisation=Cardiologie%20Interventionnelle
     */
    @GetMapping("/trajets_optimises")
    public List<UniteeSoinsTrajet> obtenirTrajetsOptimises(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("specialisation") String specialisationNom) {

        return this.uniteSoinsService.calculerTrajetsOptimises(specialisationNom, lat, lon);
    }

}