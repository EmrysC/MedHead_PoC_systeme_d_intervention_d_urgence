package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {

    private final Map<String, String> errors;

    public EmailAlreadyExistsException(Map<String, String> errors) {
        super("Un utilisatuer existe deja pour cet email");
        this.errors = errors;
    }

}
