package MeadHead.Poc.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour ignorer les méthodes de dev dans le rapport de couverture. Le
 * nom doit contenir "Generated" pour être détecté par JaCoCo.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Untested_dev_Generated {
}
