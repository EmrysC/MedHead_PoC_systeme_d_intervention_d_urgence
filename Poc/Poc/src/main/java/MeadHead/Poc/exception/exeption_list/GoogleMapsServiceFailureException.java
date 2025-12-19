package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class GoogleMapsServiceFailureException extends RuntimeException {

    private final Map<String, String> errors;

    // Constructeur standard avec une Map d'erreurs
    public GoogleMapsServiceFailureException(Map<String, String> errors) {
        super("Échec de la communication avec le service Google Maps.");
        this.errors = errors;
    }

    // Constructeur avec cause (Timeout/ erreur réseau)
    public GoogleMapsServiceFailureException(Map<String, String> errors, Throwable cause) {
        super("Échec de la communication avec le service Google Maps.", cause);
        this.errors = errors;
    }

    // Constructeur message d'erreur 
    public GoogleMapsServiceFailureException(String message) {
        super("Échec de la communication avec le service Google Maps.");
        this.errors = Map.of("detail", message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
