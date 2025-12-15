// Dans MeadHead.Poc.dto.OneOfAddressOrGps.java

package MeadHead.Poc.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TrajetRequestValidator.class) // Doit pointer vers votre validateur
@Documented
public @interface OneOfAddressOrGps {

    String message() default "Vous devez fournir soit une adresse complète, soit les coordonnées GPS (latitude et longitude), mais pas les deux.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}