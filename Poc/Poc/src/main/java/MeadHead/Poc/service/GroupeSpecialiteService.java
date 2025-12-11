package MeadHead.Poc.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MeadHead.Poc.entites.GroupeSpecialite;
import MeadHead.Poc.repository.GroupeSpecialiteRepository;
import MeadHead.Poc.dto.SpecialisationGroupeDTO;
import MeadHead.Poc.service.GroupeSpecialiteService;
import MeadHead.Poc.dto.SpecialisationOptionDTO;
import MeadHead.Poc.service.GroupeSpecialiteService;


@Service
public class GroupeSpecialiteService {

        @Autowired
        private GroupeSpecialiteRepository groupeRepository;

        public List<SpecialisationGroupeDTO> getAllSpecialiteGroupesAsDTO() {

        List<GroupeSpecialite> entities = groupeRepository.findAll();

        List<SpecialisationGroupeDTO> dtoList = entities.stream()
                .map(entity -> {

                    // Mapping des options (Specialisation entité vers SpecialisationOptionDTO)
                    List<SpecialisationOptionDTO> options = entity.getSpecialisations().stream()
                            .map(specEntity -> new SpecialisationOptionDTO(
                                    String.valueOf(specEntity.getId()),
                                    specEntity.getNom()))
                            .sorted(Comparator.comparing(SpecialisationOptionDTO::getNom))
                            .collect(Collectors.toList());

                    return new SpecialisationGroupeDTO(
                            entity.getId(),  
                            entity.getNom(), 
                            options);        
                })
                // Tri alphabétique des groupes par le champ 'nom' du DTO
                .sorted(Comparator.comparing(SpecialisationGroupeDTO::getNom))
                .collect(Collectors.toList());
        return dtoList;
    }
}