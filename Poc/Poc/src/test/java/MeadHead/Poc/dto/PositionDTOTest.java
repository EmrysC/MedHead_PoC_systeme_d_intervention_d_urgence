package MeadHead.Poc.dto;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PositionDTOTest {

    @Test
    @DisplayName("getPositionValid : Doit retourner l'adresse en priorité")
    void getPositionValid_PrioriteAdresse() {
        PositionDTO pos = new PositionDTO("10 Rue de Paris", 48.85, 2.35);
        // Même si le GPS est présent, l'adresse est retournée en premier selon ton code
        assertThat(pos.getPositionValid()).isEqualTo("10 Rue de Paris");
    }

    @Test
    @DisplayName("getPositionValid : Doit retourner le GPS si l'adresse est absente")
    void getPositionValid_RetourneGps() {
        PositionDTO pos = new PositionDTO(null, 45.0, 5.0);
        assertThat(pos.getPositionValid()).isEqualTo("45.0,5.0");
    }

    @Test
    @DisplayName("getPositionValid : Doit retourner null si rien n'est valide")
    void getPositionValid_RetourneNull() {
        PositionDTO pos = new PositionDTO("   ", null, null); // Adresse vide (trim)
        assertThat(pos.getPositionValid()).isNull();
    }

    @Test
    @DisplayName("OnlyAdresseIsValid : Test des différents scénarios classiques")
    void testOnlyAdresseIsValid() {
        // Cas : Uniquement adresse
        assertThat(new PositionDTO("Paris", null, null).OnlyAdresseIsValid()).isTrue();

        // Cas : Adresse + GPS (doit être faux car "Only" adresse)
        assertThat(new PositionDTO("Paris", 48.0, 2.0).OnlyAdresseIsValid()).isFalse();

        // Cas : Uniquement GPS
        assertThat(new PositionDTO(null, 48.0, 2.0).OnlyAdresseIsValid()).isFalse();

        // Cas : Vide
        assertThat(new PositionDTO(null, null, null).OnlyAdresseIsValid()).isFalse();
    }

    @Test
    @DisplayName("positionIsValid : Cas limites pour la couverture (GPS partiel)")
    void testPositionIsValid_PartialGps() {
        // Test de la branche : Latitude présente mais Longitude absente
        PositionDTO partialLat = new PositionDTO(null, 48.85, null);
        assertThat(partialLat.getPositionValid()).isNull();

        // Test de la branche : Longitude présente mais Latitude absente
        PositionDTO partialLong = new PositionDTO(null, null, 2.35);
        assertThat(partialLong.getPositionValid()).isNull();
    }

    @Test
    @DisplayName("OnlyAdresseIsValid : Cas où l'adresse est valide avec un GPS partiel")
    void testOnlyAdresseIsValid_PartialGps() {
        // adresseIsValid = true AND positionIsValid = false (car GPS incomplet)
        // Le résultat attendu pour "OnlyAdresse" est donc TRUE
        PositionDTO pos = new PositionDTO("Paris", 48.85, null);
        assertThat(pos.OnlyAdresseIsValid()).isTrue();
    }

    @Test
    @DisplayName("Constructeur : Doit mapper correctement depuis SpecialisationTrajetDTO")
    void testConstructorFromSpecialisationTrajetDTO() {
        // On mock le DTO source car on ne teste que le constructeur ici
        SpecialisationTrajetDTO mockSource = mock(SpecialisationTrajetDTO.class);
        when(mockSource.getAdresse()).thenReturn("Lyon");
        when(mockSource.getLatitude()).thenReturn(45.75);
        when(mockSource.getLongitude()).thenReturn(4.85);

        PositionDTO pos = new PositionDTO(mockSource);

        assertThat(pos.getAddress()).isEqualTo("Lyon");
        assertThat(pos.getLatitude()).isEqualTo(45.75);
        assertThat(pos.getLongitude()).isEqualTo(4.85);
    }
}
