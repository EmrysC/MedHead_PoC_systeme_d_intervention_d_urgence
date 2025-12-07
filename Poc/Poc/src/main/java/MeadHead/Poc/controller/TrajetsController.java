package MeadHead.Poc.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeadHead.Poc.service.TrajetsService; 
import MeadHead.Poc.UniteeSoinsTrajet;

@RestController
@RequestMapping(path = "/trajets")
public class TrajetsController {
    
    private final TrajetsService trajetsService;

    public TrajetsController(TrajetsService trajetsService) {
        this.trajetsService = trajetsService;
    }

    /**
     * Endpoint qui renvoie la liste enrichie.
     * Exemple: GET /trajets/optimises?lat=45.1&lon=5.9&specialisation=Cardiologie Interventionnelle
     */
    @GetMapping("/optimises")
    public List<UniteeSoinsTrajet> obtenirTrajetsOptimises(
        @RequestParam("lat") double lat,
        @RequestParam("lon") double lon,
        @RequestParam("specialisation") String specialisationNom) {
        
        return trajetsService.calculerTrajetsOptimises(specialisationNom, lat, lon);
    }
}