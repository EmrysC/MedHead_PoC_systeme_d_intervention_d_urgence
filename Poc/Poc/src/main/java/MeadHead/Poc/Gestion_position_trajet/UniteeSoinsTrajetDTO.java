package MeadHead.Poc.Gestion_position_trajet;

import MeadHead.Poc.entites.UniteSoins;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniteeSoinsTrajetDTO {

    private long idSpecialisation;
    private long idHopital;
    private String nomHopital;
    private int litsDisponibles;
    private DestinationCalculeeDTO destinationCalculee;

    public UniteeSoinsTrajetDTO(UniteSoins unite_soins, DestinationCalculeeDTO destinationCalculee) {
        this.idHopital = unite_soins.getHopital().getId();
        this.nomHopital = unite_soins.getHopital().getNom();
        this.idSpecialisation = unite_soins.getSpecialisation().getId();
        this.litsDisponibles = unite_soins.getLitsDisponibles();
        this.destinationCalculee = destinationCalculee;
    }

}
