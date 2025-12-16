package MeadHead.Poc.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import MeadHead.Poc.dto.SpecialisationGroupeDTO;
import MeadHead.Poc.dto.SpecialisationOptionDTO;
import MeadHead.Poc.entites.GroupeSpecialite;
import MeadHead.Poc.repository.GroupeSpecialiteRepository;

@Service
public class GroupeSpecialiteService {

    @Autowired
    private GroupeSpecialiteRepository groupeRepository;

    private static final String CACHE_SPECIALITES = "specialitesCache";

    @Cacheable(CACHE_SPECIALITES)
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

    @CacheEvict(value = CACHE_SPECIALITES, key = "'all'")
    public void clearSpecialitesCache() {
        System.out.println(">>> CACHE VIDÉ : Le cache des groupes de spécialités a été vidé.");
    } // A appelé a la MAJ des spécilitées / redemarer serveur

}
