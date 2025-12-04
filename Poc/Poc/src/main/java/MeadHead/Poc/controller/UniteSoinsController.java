package MeadHead.Poc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import MeadHead.Poc.service.UniteSoinsService;
import MeadHead.Poc.entites.UniteSoins;



@RestController
@RequestMapping(path = "unitesoins")
public class UniteSoinsController {

    private UniteSoinsService uniteSoinsService;

    public UniteSoinsController(UniteSoinsService uniteSoinsService){
        this.uniteSoinsService = uniteSoinsService;
    }


    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void creer(@RequestBody UniteSoins uniteSoins){

    }
}
