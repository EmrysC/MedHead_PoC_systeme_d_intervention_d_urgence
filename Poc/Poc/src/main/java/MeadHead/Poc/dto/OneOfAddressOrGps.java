package MeadHead.Poc.dto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TrajetRequestValidator.class)
@Documented
public @interface OneOfAddressOrGps {

    String message() default "Vous devez fournir soit une adresse complète, soit les coordonnées GPS (latitude et longitude), mais pas les deux.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
