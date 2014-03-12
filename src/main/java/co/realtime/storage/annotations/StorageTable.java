package co.realtime.storage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface StorageTable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StorageTable {

    /**
     * Name.
     * @return the string
     */
    String name() default "";

    /**
     * Primary key.
     * @return the string
     */
    String primaryKey();

    /**
     * Secondary key.
     * @return the string
     */
    String secondaryKey() default "";

}
