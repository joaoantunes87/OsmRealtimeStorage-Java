package co.realtime.storage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface StorageProperty.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StorageProperty {

    /**
     * Name.
     * @return the string
     */
    String name() default "";

    /**
     * Checks if is primary key.
     * @return true, if is primary key
     */
    boolean isPrimaryKey() default false;

    /**
     * Checks if is secondary key.
     * @return true, if is secondary key
     */
    boolean isSecondaryKey() default false;

}
