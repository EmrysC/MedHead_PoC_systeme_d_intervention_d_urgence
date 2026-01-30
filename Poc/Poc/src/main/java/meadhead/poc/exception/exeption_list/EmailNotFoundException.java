package meadhead.poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
public class EmailNotFoundException extends RuntimeException {

    private final Map<String, String> errors;

    public EmailNotFoundException(Map<String, String> errors) {
        super("Conflit de données : Email inexistant.");
        this.errors = errors;
    }

}
