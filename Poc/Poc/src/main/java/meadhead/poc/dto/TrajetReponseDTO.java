package meadhead.poc.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrajetReponseDTO {

    // infos sur la spécialisation
    private SpecialisationDetailDTO specialisationDetailDTO;

    // Le résultat du calcul des trajets
    private TrajetResultatDTO trajetsCalculesValide;
}
