package MeadHead.Poc.Gestion_position_trajet;

import MeadHead.Poc.entites.UniteSoins;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniteeSoinsTrajet {


    private String hopital;
    private String specialisation;
    private int litsDisponibles;
    private DestinationCalculee destinationCalculee;


    public UniteeSoinsTrajet(UniteSoins unite_soins, DestinationCalculee destinationCalculee) {
        this.hopital = unite_soins.getHopital().getNom();
        this.specialisation = unite_soins.getSpecialisation().getNom();
        this.litsDisponibles = unite_soins.getLitsDisponibles();
        this.destinationCalculee = destinationCalculee;
    }

}
