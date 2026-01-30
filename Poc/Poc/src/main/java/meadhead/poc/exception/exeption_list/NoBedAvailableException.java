package meadhead.poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
public class NoBedAvailableException extends RuntimeException {

    private final Map<String, String> errors;

    public NoBedAvailableException(Map<String, String> errors) {
        super("Aucun lit disponible pour cette spécialisation.");
        this.errors = errors;
    }
}
