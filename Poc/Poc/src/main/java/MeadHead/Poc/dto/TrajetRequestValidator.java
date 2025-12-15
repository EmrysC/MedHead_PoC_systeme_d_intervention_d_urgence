package MeadHead.Poc.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TrajetRequestValidator implements ConstraintValidator<OneOfAddressOrGps, SpecialisationTrajetDTO> {

    @Override
    public boolean isValid(SpecialisationTrajetDTO dto, ConstraintValidatorContext context) {

        // La validation réussit si soit si l'adresse est fournie, soit les coordonnées
        // GPS (XOR)
        final boolean dataGps = dto.getLatitude() != null && dto.getLongitude() != null;
        final boolean dataAddress = dto.getAdresse() != null && !dto.getAdresse().trim().isEmpty();

        if (dataGps && dataAddress) {
            return false;
        }

        if (dataGps) {
            return true;
        }

        if (dataAddress) {
            return true;
        }

        return false;
    }
}
