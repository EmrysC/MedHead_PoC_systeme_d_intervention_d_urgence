package MeadHead.Poc;

import java.util.List;

import MeadHead.Poc.service.TrajetsService;

public class Trajets {

    private PositionGPS originePosition;
    private List<UniteeSoinsTrajet> uniteeSoinsTrajets;

    public Trajets(PositionGPS originePosition, List<UniteeSoinsTrajet> uniteeSoinsTrajets) {
        this.originePosition = originePosition;
        this.uniteeSoinsTrajets = uniteeSoinsTrajets;
    }


}
