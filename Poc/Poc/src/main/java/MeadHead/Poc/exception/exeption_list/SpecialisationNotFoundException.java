package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class SpecialisationNotFoundException extends RuntimeException {

    private final Map<String, String> errors;

    public SpecialisationNotFoundException(Map<String, String> errors) {
        super("La spécialisation demandée n'a pas été trouvée.");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}