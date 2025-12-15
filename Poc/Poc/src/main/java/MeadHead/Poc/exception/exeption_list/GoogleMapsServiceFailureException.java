package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class GoogleMapsServiceFailureException extends RuntimeException {

    private final Map<String, String> errors;

    public GoogleMapsServiceFailureException(Map<String, String> errors) {
        super("Échec de la communication avec le service Google Maps.");
        this.errors = errors;
    }

    public GoogleMapsServiceFailureException(Map<String, String> errors, Throwable cause) {
        super("Échec de la communication avec le service Google Maps.", cause);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
