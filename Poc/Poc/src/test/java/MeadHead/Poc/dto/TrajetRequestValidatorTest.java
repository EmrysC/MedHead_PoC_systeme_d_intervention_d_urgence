package MeadHead.Poc.dto;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import jakarta.validation.ConstraintValidatorContext;

class TrajetRequestValidatorTest {

    private TrajetRequestValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new TrajetRequestValidator();

        // Mock de l'API fluide de ConstraintValidatorContext
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    @Test
    @DisplayName("isValid : Doit retourner true si le DTO est null")
    void isValid_NullDto_ReturnsTrue() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("isValid : Succès quand seul le GPS est fourni")
    void isValid_OnlyGps_ReturnsTrue() {
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(45.18)
                .longitude(5.72)
                .adresse(null)
                .build();

        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    @DisplayName("isValid : Succès quand seule l'adresse est fournie")
    void isValid_OnlyAddress_ReturnsTrue() {
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(null)
                .longitude(null)
                .adresse("10 Rue de Paris, 75001 Paris")
                .build();

        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    @DisplayName("isValid : Échec si GPS et Adresse sont fournis (Conflit)")
    void isValid_BothProvided_ReturnsFalse() {
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(45.0)
                .longitude(5.0)
                .adresse("Lyon")
                .build();

        boolean result = validator.isValid(dto, context);

        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(contains("Trop d'informations"));
        verify(violationBuilder).addPropertyNode("methode_localisation");
    }

    @Test
    @DisplayName("isValid : Échec si aucune donnée n'est fournie")
    void isValid_NoneProvided_ReturnsFalse() {
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(null)
                .longitude(null)
                .adresse(null)
                .build();

        boolean result = validator.isValid(dto, context);

        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate(contains("Données manquantes"));
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("isValid : Échec si l'adresse ne contient que des espaces")
    void isValid_BlankAddress_ReturnsFalse() {
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .adresse("   ") // trim() doit détecter que c'est vide
                .build();

        assertThat(validator.isValid(dto, context)).isFalse();
    }

    // --- TESTS POUR LA COUVERTURE COMPLÈTE DU GPS PARTIEL ---
    @Test
    @DisplayName("isValid : GPS partiel (Latitude seule) sans adresse - Échec")
    void isValid_LatitudeOnly_NoAddress_ReturnsFalse() {
        // dataGps est FALSE car longitude est nulle
        // dataAddress est FALSE
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(45.0)
                .longitude(null)
                .adresse(null)
                .build();

        assertThat(validator.isValid(dto, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate(contains("Données manquantes"));
    }

    @Test
    @DisplayName("isValid : GPS partiel (Longitude seule) sans adresse - Échec")
    void isValid_LongitudeOnly_NoAddress_ReturnsFalse() {
        // dataGps est FALSE car latitude est nulle
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(null)
                .longitude(5.0)
                .adresse(null)
                .build();

        assertThat(validator.isValid(dto, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate(contains("Données manquantes"));
    }

    @Test
    @DisplayName("isValid : GPS incomplet avec Adresse valide - Succès")
    void isValid_PartialGps_WithValidAddress_ReturnsTrue() {
        // dataGps est FALSE (un seul champ GPS)
        // dataAddress est TRUE
        // Aucun bloc IF n'est déclenché -> return true final
        SpecialisationTrajetDTO dto = SpecialisationTrajetDTO.builder()
                .latitude(45.0)
                .longitude(null)
                .adresse("1 Avenue de la République, Paris")
                .build();

        assertThat(validator.isValid(dto, context)).isTrue();
    }
}
