package MeadHead.Poc.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MeadHead.Poc.dto.SpecialisationGroupeDTO;
import MeadHead.Poc.service.GroupeSpecialiteService;



@RestController
@RequestMapping("specilites")
public class GroupeSpecialiteController {

    private final GroupeSpecialiteService groupeSpecialiteService;

public GroupeSpecialiteController(GroupeSpecialiteService groupeSpecialiteService) {
        this.groupeSpecialiteService = groupeSpecialiteService;
    }

    @GetMapping
    public List<SpecialisationGroupeDTO> getSpecialitesGroupes() { 
        return groupeSpecialiteService.getAllSpecialiteGroupesAsDTO();
    }
}
