package co.realtime.storage.annotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONObject;

import co.realtime.storage.ItemAttribute;
import co.realtime.storage.utils.TypeValidatorUtils;

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
    public static void mapAttributesToInstance(final Object instance, final Map<String, Object> attributes) {

        final Field[] fields = instance.getClass().getDeclaredFields();

        for (final Field f : fields) {

            if (isFieldStorageProperty(f)) {

                final Object value = calculateValueToField(f, attributes);

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
     * @param instance
     *            the instance
     * @return the map
     */
    public static Map<String, ItemAttribute> instanceToAttributes(final Object instance) {

        final HashMap<String, ItemAttribute> attributes = new LinkedHashMap<>(0);
        final Field[] fields = instance.getClass().getDeclaredFields();

        for (final Field f : fields) {

            if (isFieldStorageProperty(f)) {

                final Map<String, ItemAttribute> values = buildItemAttributesFromField(f, instance);
                attributes.putAll(values);

            }

        }

        return attributes;

    }

    /**
     * Builds the item attribute from field.
     * @param f
     *            the f
     * @param instance
     *            the instance
     * @return the item attribute
     */
    private static Map<String, ItemAttribute> buildItemAttributesFromField(final Field f, final Object instance) {

        try {
            f.setAccessible(true);
            final Object value = f.get(instance);
            if (value != null) {
                return buildItemAttributesFromValue(f, prepareFieldToStorage(f, instance));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Collections.emptyMap();

    }

    /**
     * Convert field to object.
     * @param f
     *            the f
     * @param instance
     *            the instance
     * @return the object
     */
    private static Object convertFieldToObject(final Field f, final Object instance) {

        f.setAccessible(true);
        Object value = null;

        try {
            value = f.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (value == null) {
            return null;
        }

        if (f.isAnnotationPresent(StorageProperty.class)) {
            return value;
        } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {
            return value.toString();
        } else if (f.isAnnotationPresent(JsonStorageProperty.class)) {
            return convertJsonStoragePropertyToJson(value);
        } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class) && value instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) value;
            final JSONArray jsonArray = new JSONArray();
            for (final Object item : collection) {
                final JSONObject jsonField = convertJsonStoragePropertyToJson(item);
                jsonArray.put(jsonField);
            }
            return jsonArray;
        } else if (f.isAnnotationPresent(EmbeddedStorageProperties.class)) {
            return value;
        }

        return null;

    }

    /**
     * Prepare field to storage.
     * @param f
     *            the f
     * @param instance
     *            the instance
     * @return the object
     */
    private static Object prepareFieldToStorage(final Field f, final Object instance) {

        Object value = null;
        if (instance != null && f != null) {

            value = convertFieldToObject(f, instance);
            if (value != null) {

                if (f.isAnnotationPresent(JsonStorageProperty.class) || f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {
                    return value.toString();
                }

            }

        }

        return value;

    }

    /**
     * Builds the item attribute from value.
     * @param f
     *            the f
     * @param value
     *            the value
     * @return the item attribute
     */
    private static Map<String, ItemAttribute> buildItemAttributesFromValue(final Field f, final Object value) {

        Map<String, ItemAttribute> attributes = new HashMap<>();

        if (value != null) {

            // is a Number
            if (value instanceof Number) {
                final String propertyName = calculatePropertyNameField(f);
                if (propertyName != null && !propertyName.isEmpty()) {
                    attributes.put(propertyName, new ItemAttribute((Number) value));
                }
                // is not a number but is a primitive value or a String
            } else if (TypeValidatorUtils.isWrapperType(value.getClass()) || value instanceof String) {
                final String propertyName = calculatePropertyNameField(f);
                if (propertyName != null && !propertyName.isEmpty()) {
                    attributes.put(propertyName, new ItemAttribute(value.toString()));
                }
                // is a complex object
            } else {
                attributes = instanceToAttributes(value);
            }

        }

        return attributes;

    }

    /**
     * Convert json storage property to json.
     * @param object
     *            the object
     * @return the jSON object
     */
    private static JSONObject convertJsonStoragePropertyToJson(final Object object) {

        final JSONObject jsonObject = new JSONObject();

        final Field[] itemFields = object.getClass().getDeclaredFields();
        for (final Field itemField : itemFields) {

            final String propertyName = calculatePropertyNameField(itemField);
            jsonObject.put(propertyName, convertFieldToObject(itemField, object));

        }

        return jsonObject;

    }

    /**
     * Calculate value to field.
     * @param f
     *            the f
     * @param attributes
     *            the attributes
     * @return the object
     */
    public static Object calculateValueToField(final Field f, final Map<String, Object> attributes) {

        Object value = null;

        final String propertyName = calculatePropertyNameField(f);
        Object attribute = null;
        if (propertyName != null && !propertyName.isEmpty() && attributes.containsKey(propertyName)) {
            attribute = attributes.get(propertyName);
        }

        if (f.isAnnotationPresent(StorageProperty.class)) {
            value = attribute == null ? null : calculateValueToSimpleStorageProperty(f, attribute);
        } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {
            value = attribute == null ? null : calculateValueToStoragePropertyEnum(f, attribute);
        } else if (f.isAnnotationPresent(JsonStorageProperty.class)) {
            value = attribute == null ? null : calculateValueToJsonStorageProperty(f, attribute.toString());
        } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {
            final JsonCollectionStorageProperty jsonCollectionStoragePropertyAnnotation = f.getAnnotation(JsonCollectionStorageProperty.class);
            final Class<?> klass = jsonCollectionStoragePropertyAnnotation.klass();
            value = attribute == null ? null : calculateValueToJsonCollectionStorageProperty(f, (String) attribute, klass);
        } else if (f.isAnnotationPresent(EmbeddedStorageProperties.class)) {
            final Class<?> klass = f.getType();
            try {
                value = klass.newInstance();
                mapAttributesToInstance(value, attributes);
            } catch (InstantiationException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        } else if (f.isAnnotationPresent(JsonStorageProperty.class)) {

            final JsonStorageProperty jsonStoragePropertyAnnotation = f.getAnnotation(JsonStorageProperty.class);
            if (jsonStoragePropertyAnnotation.name() != null && !jsonStoragePropertyAnnotation.name().isEmpty()) {
                propertyName = jsonStoragePropertyAnnotation.name();
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
     * Checks if is field storage property.
     * @param f
     *            the f
     * @return true, if is field storage property
     */
    private static boolean isFieldStorageProperty(final Field f) {

        if (f == null) {
            return false;
        }

        return f.isAnnotationPresent(StorageProperty.class) || f.isAnnotationPresent(StoragePropertyEnum.class) || f.isAnnotationPresent(JsonStorageProperty.class) || f.isAnnotationPresent(JsonCollectionStorageProperty.class) || f.isAnnotationPresent(EmbeddedStorageProperties.class);

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
     * Calculate value to json storage property.
     * @param f
     *            the f
     * @param jsonItemsAsString
     *            the json items as string
     * @return the object converted to an instance of the f type. Null if cannot instatiate an object of f type
     */
    private static Object calculateValueToJsonStorageProperty(final Field f, final String jsonItemsAsString) {

        final JSONObject jsonItem = new JSONObject(jsonItemsAsString);
        final Class<?> klass = f.getType();

        return calculateValueFromJsonObject(jsonItem, klass);

    }

    /**
     * Calculate value from json object.
     * @param jsonObject
     *            the json object
     * @param itemClass
     *            the item class
     * @return an instance of itemClass with json data. null if wasn't possible to instantiate an object of itemClass
     */
    private static Object calculateValueFromJsonObject(final JSONObject jsonObject, final Class<?> itemClass) {

        Object itemInstance = null;

        try {

            itemInstance = itemClass.newInstance();
            final Field[] itemFields = itemClass.getDeclaredFields();

            for (final Field itemField : itemFields) {

                try {

                    final String propertyName = calculatePropertyNameField(itemField);

                    if (jsonObject.has(propertyName)) {

                        final Object attribute = jsonObject.get(propertyName);
                        final Map<String, Object> attributes = new HashMap<>();
                        attributes.put(propertyName, attribute);

                        if (attribute != null) {
                            final Object value = calculateValueToField(itemField, attributes);
                            itemField.setAccessible(true);
                            itemField.set(itemInstance, value);
                        }

                    }

                } catch (final IllegalAccessException exception) {
                    // TODO Auto-generated catch block
                    exception.printStackTrace();
                }

            }

        } catch (final InstantiationException | IllegalAccessException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
        }

        return itemInstance;

    }

    /**
     * Calculate value to json collection storage property.
     * @param f
     *            the f
     * @param jsonItemsAsString
     *            the json items as string
     * @param itemsClass
     *            the items class
     * @return the list
     */
    private static List<Object> calculateValueToJsonCollectionStorageProperty(final Field f, final String jsonItemsAsString, final Class<?> itemsClass) {

        final List<Object> items = new ArrayList<>();

        if (jsonItemsAsString != null && !jsonItemsAsString.isEmpty()) {

            final JSONArray jsonItems = new JSONArray(jsonItemsAsString);
            for (int i = 0; i < jsonItems.length(); i++) {

                final JSONObject jsonItem = jsonItems.getJSONObject(i);
                final Object itemInstance = calculateValueFromJsonObject(jsonItem, itemsClass);

                if (itemInstance != null) {
                    items.add(itemInstance);
                }

            }

        }

        return items;

    }

}
