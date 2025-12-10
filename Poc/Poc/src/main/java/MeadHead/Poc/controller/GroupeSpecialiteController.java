package MeadHead.Poc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MeadHead.Poc.dto.SpecialisationGroupeDTO;
import MeadHead.Poc.service.GroupeSpecialiteService;
import MeadHead.Poc.dto.SpecialisationOptionDTO;
import MeadHead.Poc.dto.SpecialisationOptionDTO;

import MeadHead.Poc.dto.ReservationRequestDTO;
import MeadHead.Poc.entites.Specialisation;
import MeadHead.Poc.service.ReservationService;
import jakarta.persistence.OneToMany;
import lombok.RequiredArgsConstructor;
import java.util.List;

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
