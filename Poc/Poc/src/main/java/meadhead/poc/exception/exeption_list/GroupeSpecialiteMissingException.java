package meadhead.poc.exception.exeption_list;

import java.util.Map;

import lombok.Getter;

@Getter
public class GroupeSpecialiteMissingException extends RuntimeException {

    private final Map<String, String> errors;

    public GroupeSpecialiteMissingException(Map<String, String> errors) {
        super("Erreur de cohérence interne : La spécialisation n'est pas associée à un groupe valide.");
        this.errors = errors;
    }

}
