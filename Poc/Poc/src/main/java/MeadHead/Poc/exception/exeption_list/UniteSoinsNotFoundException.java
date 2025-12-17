package MeadHead.Poc.exception.exeption_list;

public class UniteSoinsNotFoundException extends RuntimeException {

    public UniteSoinsNotFoundException(Long uniteSoinsId) {
        super(String.format("L'unité de soins avec l'ID '%d' est introuvable.", uniteSoinsId));
    }
}
