package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class EmailNotFoundException extends RuntimeException {

    private final Map<String, String> errors;

    public EmailNotFoundException(Map<String, String> errors) {
        super("Conflit de données : Email inexistant.");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}