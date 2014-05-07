package co.realtime.storage.annotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONObject;

import co.realtime.storage.ItemAttribute;
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

            if (isFieldStorageProperty(f)) {

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

    }

    /**
     * Active record to attributes.
     * @param record
     *            the record
     * @return the map
     */
    public static Map<String, ItemAttribute> activeRecordToAttributes(final ActiveRecord record) {

        final HashMap<String, ItemAttribute> attributes = new LinkedHashMap<>(0);
        final Field[] fields = record.getClass().getDeclaredFields();

        for (final Field f : fields) {

            if (isFieldStorageProperty(f)) {

                final String propertyName = calculatePropertyNameField(f);
                if (propertyName != null && !propertyName.isEmpty()) {

                    try {

                        final ItemAttribute value = buildItemAttributeFromField(f, record);
                        if (value != null) {
                            attributes.put(propertyName, value);
                        }

                    } catch (final IllegalArgumentException illegalArgumentException) {
                        // TODO Auto-generated catch block
                        illegalArgumentException.printStackTrace();
                    }

                }

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
    private static ItemAttribute buildItemAttributeFromField(final Field f, final Object instance) {

        try {
            f.setAccessible(true);
            final Object value = f.get(instance);
            if (value != null) {
                return buildItemAttributeFromValue(convertFieldToObject(f, instance));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

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
        }

        return null;

    }

    /**
     * Builds the item attribute from value.
     * @param value
     *            the value
     * @return the item attribute
     */
    private static ItemAttribute buildItemAttributeFromValue(final Object value) {

        if (value != null) {
            if (value instanceof Number) {
                return new ItemAttribute((Number) value);
            }
            return new ItemAttribute(value.toString());
        }

        return null;

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
        } else if (f.isAnnotationPresent(JsonStorageProperty.class)) {
            value = calculateValueToJsonStorageProperty(f, attribute.toString());
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

        if (f.isAnnotationPresent(StorageProperty.class) || f.isAnnotationPresent(StoragePropertyEnum.class) || f.isAnnotationPresent(JsonStorageProperty.class) || f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {
            return true;
        }

        return false;

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

                        if (attribute != null) {
                            final Object value = calculateValueToField(itemField, attribute);
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
