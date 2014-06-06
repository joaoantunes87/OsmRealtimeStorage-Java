package co.realtime.storage.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class TypeValidatorUtils.
 */
public class TypeValidatorUtils {

    /** The Constant WRAPPER_TYPES. */
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    /**
     * Checks if is wrapper type.
     * @param clazz
     *            the clazz
     * @return true, if is wrapper type
     */
    public static boolean isWrapperType(final Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    /**
     * Gets the wrapper types.
     * @return the wrapper types
     */
    private static Set<Class<?>> getWrapperTypes() {
        final Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

}
