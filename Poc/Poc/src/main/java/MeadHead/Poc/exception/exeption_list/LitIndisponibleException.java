package MeadHead.Poc.exception.exeption_list;

import lombok.Getter;

@Getter
public class LitIndisponibleException extends RuntimeException {

    public LitIndisponibleException(Long uniteSoinsId) {
        super(String.format("Impossible de réserver : plus de lits disponibles dans l'unité de soins ID '%d'.", uniteSoinsId));
    }
}
