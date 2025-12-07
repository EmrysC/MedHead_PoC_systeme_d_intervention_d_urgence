package MeadHead.Poc;

import MeadHead.Poc.entites.UniteSoins;

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

    public String getHopital() {
        return hopital;
    }
    public String getSpecialisation() {
        return specialisation;
    }
    public int getLitsDisponibles() {
        return litsDisponibles;
    }
    public DestinationCalculee getDestinationCalculee() {
        return destinationCalculee;
    }


}
