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



import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;




@RestController
@RequestMapping(path = "unitesoins")
public class UniteSoinsController {

    private UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsController(UniteSoinsRepository uniteSoinsRepository){
        this.uniteSoinsRepository = uniteSoinsRepository;
    }


    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void creer(@RequestBody UniteSoins uniteSoins){
        uniteSoinsRepository.save(uniteSoins);
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public UniteSoins trouverParId(@PathVariable Long id){
        return this.uniteSoinsRepository.findById(id).orElse(null);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> listerTous() {
        return this.uniteSoinsRepository.findAll();
    }

    @GetMapping(path = "/recherche_lit_dispo", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UniteSoins> rechercherLitDisponible(@RequestParam("specialisation") String nomSpecialisation){ 
        return this.uniteSoinsRepository.findBySpecialisationNomAndLitsDisponiblesGreaterThan(
            nomSpecialisation, 
            0 
        );
    }

}