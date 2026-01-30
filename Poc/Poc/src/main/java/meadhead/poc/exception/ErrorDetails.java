package meadhead.poc.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Modèle pour une réponse d'erreur API standard (JSON).
public class ErrorDetails {

    private LocalDateTime timestamp;
    private String message;
    private Map<String, String> errors; // les eurreur DTO sont sous ce format
    private String path; // Correspond au endpoint

}