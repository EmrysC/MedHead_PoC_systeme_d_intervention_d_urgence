package MeadHead.Poc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;


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
}
