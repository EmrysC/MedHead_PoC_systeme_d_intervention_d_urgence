package MeadHead.Poc.exception.exeption_list;

import org.springframework.validation.BindingResult;

public class ValidationManuelleException extends RuntimeException {

    private final BindingResult bindingResult;

    public ValidationManuelleException(BindingResult bindingResult) {
        super("Erreur de validation manuelle");
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
