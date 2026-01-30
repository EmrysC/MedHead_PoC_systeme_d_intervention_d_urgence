package meadhead.poc.dto;

import meadhead.poc.entites.Specialisation;
import lombok.*;

// DTO qui encapsule les informations essentielles de la Spécialisation et de son Groupe associé.
//Utilisé pour enrichir les réponses API.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationDetailDTO {

    // Informations sur le Groupe de Spécialité
    private Long groupeSpecialiteId;
    private String groupeSpecialiteNom;

    // Informations sur la Spécialisation
    private Long specialisationId;
    private String specialisationNom;

    public SpecialisationDetailDTO(Specialisation specialisation) {

        this.groupeSpecialiteId = specialisation.getGroupeSpecialite().getId();
        this.groupeSpecialiteNom = specialisation.getGroupeSpecialite().getNom();

        this.specialisationId = specialisation.getId();
        this.specialisationNom = specialisation.getNom();
    }

}
