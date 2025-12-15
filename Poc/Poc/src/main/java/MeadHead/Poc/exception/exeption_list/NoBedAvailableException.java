package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class NoBedAvailableException extends RuntimeException {

    private final Map<String, String> errors;

    public NoBedAvailableException(Map<String, String> errors) {
        super("Aucun lit disponible pour cette spécialisation.");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
