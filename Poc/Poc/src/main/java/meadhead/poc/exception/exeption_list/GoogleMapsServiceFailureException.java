package meadhead.poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
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

}
