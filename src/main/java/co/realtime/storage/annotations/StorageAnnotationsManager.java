package co.realtime.storage.annotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONObject;

import co.realtime.storage.models.ActiveRecord;

/**
 * The Class StorageAnnotationsManager.
 */
public class StorageAnnotationsManager {

    /**
     * Map attributes to instance.
     * @param instance
     *            the instance
     * @param attributes
     *            the attributes
     */
    public static void mapAttributesToInstance(final ActiveRecord instance, final Map<String, Object> attributes) {

        final Field[] fields = instance.getClass().getDeclaredFields();

        for (final Field f : fields) {

            final String propertyName = calculatePropertyNameField(f);
            if (propertyName != null && !propertyName.isEmpty() && attributes.containsKey(propertyName)) {

                final Object attribute = attributes.get(propertyName);
                final Object value = calculateValueToField(f, attribute);

                if (value != null) {

                    f.setAccessible(true);

                    try {
                        f.set(instance, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }

        }

    }

    /**
     * Active record to attributes.
     * @param record
     *            the record
     * @return the map
     */
    public static Map<String, Object> activeRecordToAttributes(final ActiveRecord record) {

        final HashMap<String, Object> attributes = new LinkedHashMap<>(0);
        final Field[] fields = record.getClass().getDeclaredFields();

        for (final Field f : fields) {

            final String propertyName = calculatePropertyNameField(f);
            if (propertyName != null && !propertyName.isEmpty() && attributes.containsKey(propertyName)) {

                final Object attribute = attributes.get(propertyName);
                final Object value = calculateValueToField(f, attribute);

                if (value != null) {
                    attributes.put(propertyName, value);
                }

            }

        }

        return attributes;

    }

    /**
     * Calculate value to field.
     * @param f
     *            the f
     * @param attribute
     *            the attribute
     * @return the object
     */
    public static Object calculateValueToField(final Field f, final Object attribute) {

        Object value = null;

        if (f.isAnnotationPresent(StorageProperty.class)) {
            value = calculateValueToSimpleStorageProperty(f, attribute);
        } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {
            value = calculateValueToStoragePropertyEnum(f, attribute);
        } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {
            final JsonCollectionStorageProperty jsonCollectionStoragePropertyAnnotation = f.getAnnotation(JsonCollectionStorageProperty.class);
            final Class<?> klass = jsonCollectionStoragePropertyAnnotation.klass();
            value = calculateValueToJsonCollectionStorageProperty(f, (String) attribute, klass);
        }

        return value;

    }

    /**
     * Calculate property name field.
     * @param f
     *            the f
     * @return the string
     */
    private static String calculatePropertyNameField(final Field f) {

        // TODO improve it: DRY

        String propertyName = f.getName();
        if (f.isAnnotationPresent(StorageProperty.class)) {

            final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
            if (storagePropertyAnnotation.name() != null && !storagePropertyAnnotation.name().isEmpty()) {
                propertyName = storagePropertyAnnotation.name();
            }

        } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {

            final StoragePropertyEnum storagePropertyEnumAnnotation = f.getAnnotation(StoragePropertyEnum.class);
            if (storagePropertyEnumAnnotation.name() != null && !storagePropertyEnumAnnotation.name().isEmpty()) {
                propertyName = storagePropertyEnumAnnotation.name();
            }

        } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {

            final JsonCollectionStorageProperty jsonCollectionStoragePropertyAnnotation = f.getAnnotation(JsonCollectionStorageProperty.class);
            if (jsonCollectionStoragePropertyAnnotation.name() != null && !jsonCollectionStoragePropertyAnnotation.name().isEmpty()) {
                propertyName = jsonCollectionStoragePropertyAnnotation.name();
            }

        }

        return propertyName;

    }

    /**
     * Calculate value to simple storage property.
     * @param f
     *            the f
     * @param attribute
     *            the attribute
     * @return the object
     */
    private static Object calculateValueToSimpleStorageProperty(final Field f, final Object attribute) {

        if (attribute != null) {

            final Class<?> fieldType = f.getType();
            final String fieldClassName = fieldType.getSimpleName();

            if (attribute instanceof Number) {

                final Number attributeNumber = (Number) attribute;
                switch (fieldClassName) {
                case "Long":
                    return new Long(attributeNumber.longValue());
                case "Byte":
                    return new Byte(attributeNumber.byteValue());
                case "Double":
                    return new Double(attributeNumber.doubleValue());
                case "Float":
                    return new Float(attributeNumber.floatValue());
                case "Integer":
                    return new Integer(attributeNumber.intValue());
                case "Short":
                    return new Short(attributeNumber.shortValue());
                case "BigDecimal":
                    return new BigDecimal(attributeNumber.doubleValue());
                case "AtomicInteger":
                    return new AtomicInteger(attributeNumber.intValue());
                case "AtomicLong":
                    return new AtomicLong(attributeNumber.longValue());
                case "String":
                    return attributeNumber.toString();
                default:
                    break;
                }

            } else if (fieldClassName.equals("Boolean")) {
                return Boolean.valueOf(attribute.toString());
            }

        }

        return attribute;

    }

    /**
     * Calculate value to storaget property enum.
     * @param f
     *            the f
     * @param attribute
     *            the attribute
     * @return the enum
     */
    private static Enum<?> calculateValueToStoragePropertyEnum(final Field f, final Object attribute) {

        if (attribute == null) {
            return null;
        }

        return Enum.valueOf(((Class<? extends Enum>) f.getType()), attribute.toString());

    }

    /**
     * Calculate value to json collection storage property.
     * @param f
     *            the f
     * @param jsonItemsAsString
     *            the json items as string
     * @param itemsKlass
     *            the items klass
     * @return the list
     */
    private static List<Object> calculateValueToJsonCollectionStorageProperty(final Field f, final String jsonItemsAsString, final Class<?> itemsKlass) {

        final List<Object> items = new ArrayList<>();
        if (jsonItemsAsString != null && !jsonItemsAsString.isEmpty()) {

            final JSONArray jsonItems = new JSONArray(jsonItemsAsString);
            for (int i = 0; i < jsonItems.length(); i++) {

                try {

                    final JSONObject jsonItem = jsonItems.getJSONObject(i);
                    final Object itemInstance = itemsKlass.newInstance();
                    final Field[] itemFields = itemsKlass.getDeclaredFields();

                    for (final Field itemField : itemFields) {

                        try {

                            final String propertyName = calculatePropertyNameField(itemField);

                            if (jsonItem.has(propertyName)) {

                                final Object attribute = jsonItem.get(propertyName);
                                final Object value = calculateValueToField(itemField, attribute);

                                if (attribute != null) {
                                    itemField.setAccessible(true);
                                    itemField.set(itemInstance, value);
                                }

                            }

                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                    items.add(itemInstance);

                } catch (InstantiationException | IllegalAccessException exception) {
                    // TODO Auto-generated catch block
                    exception.printStackTrace();
                }

            }

        }

        return items;

    }

}
