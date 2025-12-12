package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class EmailAlreadyExistsException extends RuntimeException {

    private final Map<String, String> errors;

    public EmailAlreadyExistsException(Map<String, String> errors) {
        super("Un utilisatuer existe deja pour cet email");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}