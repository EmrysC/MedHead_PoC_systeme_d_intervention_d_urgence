package meadhead.poc.dto;

import java.util.List;

import meadhead.poc.gestion_position_trajet.UniteeSoinsTrajetDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder // Simplifie la construction de l'objet
public class TrajetResultatDTO {

    // L'objet PositionDTO de l'origine
    private PositionDTO originePosition;

    // La liste des unités de soins disponibles enrichies avec les données de trajet
    private List<UniteeSoinsTrajetDTO> unitesSoinsTrajets;

}
