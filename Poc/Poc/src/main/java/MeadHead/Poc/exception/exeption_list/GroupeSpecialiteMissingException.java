package MeadHead.Poc.exception.exeption_list;

import java.util.Map;

public class GroupeSpecialiteMissingException extends RuntimeException {

    private final Map<String, String> errors;

    public GroupeSpecialiteMissingException(Map<String, String> errors) {
        super("Erreur de cohérence interne : La spécialisation n'est pas associée à un groupe valide.");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}