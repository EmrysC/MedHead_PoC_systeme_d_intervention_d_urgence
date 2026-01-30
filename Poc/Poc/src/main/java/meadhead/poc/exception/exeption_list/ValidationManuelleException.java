package meadhead.poc.exception.exeption_list;

import org.springframework.validation.BindingResult;

import lombok.Getter;

@Getter
public class ValidationManuelleException extends RuntimeException {

    private final BindingResult bindingResult;

    public ValidationManuelleException(BindingResult bindingResult) {
        super("Erreur de validation manuelle");
        this.bindingResult = bindingResult;
    }

    public ValidationManuelleException(String message) {
        super(message);
        this.bindingResult = null;
    }
}
