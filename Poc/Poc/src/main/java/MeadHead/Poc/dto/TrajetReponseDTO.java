package MeadHead.Poc.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrajetReponseDTO {

    // infos sur la spécialisation
    private SpecialisationDetailDTO specialisationDetailDTO;

    // Le résultat du calcul des trajets
    private TrajetResultatDTO trajetsCalculesValide;
}