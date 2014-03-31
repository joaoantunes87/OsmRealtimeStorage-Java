package co.realtime.storage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface StoragePropertyEnum.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StoragePropertyEnum {

    /**
     * Name.
     * @return the string
     */
    String name() default "";
}
