package meadhead.poc.service;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import meadhead.poc.dto.SpecialisationDetailDTO;
import meadhead.poc.entites.Specialisation;
import meadhead.poc.exception.exeption_list.GroupeSpecialiteMissingException;
import meadhead.poc.exception.exeption_list.SpecialisationNotFoundException;
import meadhead.poc.repository.SpecialisationRepository;

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
