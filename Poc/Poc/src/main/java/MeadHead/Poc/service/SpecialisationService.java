package MeadHead.Poc.service;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import MeadHead.Poc.dto.SpecialisationDetailDTO;
import MeadHead.Poc.entites.Specialisation;
import MeadHead.Poc.exception.exeption_list.GroupeSpecialiteMissingException;
import MeadHead.Poc.exception.exeption_list.SpecialisationNotFoundException;
import MeadHead.Poc.repository.SpecialisationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialisationService {

    private final SpecialisationRepository specialisationRepository;

    public SpecialisationDetailDTO getSpecialisationDetailsDTO(@NonNull Long idSpecialisation) {

        Specialisation specialisation = specialisationRepository.findById(idSpecialisation)
                .orElseThrow(() -> new SpecialisationNotFoundException(
                Map.of("specialisationId",
                        String.format("Aucune spécialisation trouvée avec l'ID: %d", idSpecialisation))));

        if (specialisation.getGroupeSpecialite() == null) {
            throw new GroupeSpecialiteMissingException(
                    Map.of("groupeSpecialite",
                            String.format(
                                    "La spécialisation avec l'ID: %d est invalide car elle n'est associée à aucun groupe.",
                                    idSpecialisation)));
        }

        // Mappe et retourne le DTO en utilisant le constructeur du
        // SpecialisationDetailDTO
        return new SpecialisationDetailDTO(specialisation);
    }
}
