package MeadHead.Poc.Gestion_position_trajet;

import MeadHead.Poc.entites.UniteSoins;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniteeSoinsTrajetDTO {

    private long idHopital;
    private long idSpecialisation;
    private int litsDisponibles;
    private DestinationCalculeeDTO destinationCalculee;

    public UniteeSoinsTrajetDTO(UniteSoins unite_soins, DestinationCalculeeDTO destinationCalculee) {
        this.idHopital = unite_soins.getHopital().getId();
        this.idSpecialisation = unite_soins.getSpecialisation().getId();
        this.litsDisponibles = unite_soins.getLitsDisponibles();
        this.destinationCalculee = destinationCalculee;
    }

}
