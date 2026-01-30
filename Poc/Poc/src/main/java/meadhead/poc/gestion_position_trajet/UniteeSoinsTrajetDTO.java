package meadhead.poc.gestion_position_trajet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import meadhead.poc.entites.UniteSoins;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniteeSoinsTrajetDTO {

    private long idUniteSoins;
    private long idHopital;
    private String nomHopital;
    private int litsDisponibles;
    private DestinationCalculeeDTO destinationCalculee;

    public UniteeSoinsTrajetDTO(UniteSoins uniteSoins, DestinationCalculeeDTO destinationCalculee) {
        this.idHopital = uniteSoins.getHopital().getId();
        this.nomHopital = uniteSoins.getHopital().getNom();
        this.idUniteSoins = uniteSoins.getId();
        this.litsDisponibles = uniteSoins.getLitsDisponibles();
        this.destinationCalculee = destinationCalculee;
    }

}
