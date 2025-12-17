package MeadHead.Poc.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TrajetRequestValidator implements ConstraintValidator<OneOfAddressOrGps, SpecialisationTrajetDTO> {

    @Override
    public boolean isValid(SpecialisationTrajetDTO dto, ConstraintValidatorContext context) {

        // Si l'objet est nul, la validation @NotNull s'en occupe ailleurs
        if (dto == null) {
            return true;
        }

        // La validation réussit si soit si l'adresse est fournie, soit les coordonnées
        // GPS (XOR car on ne serrait pas quelle donnée prendre)
        final boolean dataGps = dto.getLatitude() != null && dto.getLongitude() != null;
        final boolean dataAddress = dto.getAdresse() != null && !dto.getAdresse().trim().isEmpty();

        if (dataGps && dataAddress) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Trop d'informations : Veuillez fournir soit l'adresse, soit le GPS, mais pas les deux.")
                    .addPropertyNode("methode_localisation")
                    .addConstraintViolation();
            return false;
        }

        if (!dataGps && !dataAddress) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Données manquantes : Vous devez fournir au moins une adresse complète ou des coordonnées GPS.")
                    .addPropertyNode("methode_localisation")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
