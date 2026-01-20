package MeadHead.Poc.gestion_position_trajet;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import MeadHead.Poc.gestion_position_trajet.PositionGPS;

class PositionGPSTest {

    @Test
    @DisplayName("Test de la méthode toString personnalisée")
    void testToString() {
        // Given
        PositionGPS position = new PositionGPS(43.2965, 5.3698);

        // When
        String result = position.toString();

        // Then
        // Le format attendu est "latitude,longitude" selon l'implémentation
        assertThat(result).isEqualTo("43.2965,5.3698");
    }
}
