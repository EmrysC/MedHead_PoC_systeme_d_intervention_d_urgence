package meadhead.poc.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annotation pour ignorer les méthodes de dev dans le rapport de couverture. 
// Usage exemple :
//          @Untested_dev_Generated 
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.METHOD,
    ElementType.TYPE,
    ElementType.CONSTRUCTOR
})
public @interface Untested_dev_Generated {
}
