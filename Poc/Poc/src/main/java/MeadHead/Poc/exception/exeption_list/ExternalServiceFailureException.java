package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
public class ExternalServiceFailureException extends RuntimeException {

    private final Map<String, String> errors;

    public ExternalServiceFailureException(Map<String, String> errors) {
        super("Le service de calcul de trajets optimisés est indisponible ou a échoué.");
        this.errors = errors;
    }

}
