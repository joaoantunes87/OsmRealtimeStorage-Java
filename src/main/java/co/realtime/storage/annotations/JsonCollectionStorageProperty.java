package co.realtime.storage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface JsonCollectionStorageProperty.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonCollectionStorageProperty {

    /**
     * Name.
     * @return the string
     */
    String name();

    /**
     * Klass.
     * @return the class
     */
    Class<?> klass() default Object.class;
}
