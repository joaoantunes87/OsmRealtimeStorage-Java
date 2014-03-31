package co.realtime.storage.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemRef;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.StorageRef;
import co.realtime.storage.StorageRef.StorageDataType;
import co.realtime.storage.TableRef;
import co.realtime.storage.annotations.JsonCollectionStorageProperty;
import co.realtime.storage.annotations.StorageProperty;
import co.realtime.storage.annotations.StoragePropertyEnum;
import co.realtime.storage.annotations.StorageTable;
import co.realtime.storage.api.Error;
import co.realtime.storage.api.OnErrorCommand;
import co.realtime.storage.api.OnSuccessCollectionCommand;
import co.realtime.storage.api.OnSuccessRecordCommand;
import co.realtime.storage.api.QueryRef;
import co.realtime.storage.async.ActiveRecordStateFuture;
import co.realtime.storage.async.ActiveRecordsCollectionStateFuture;
import co.realtime.storage.connection.StorageRefFactorySingleton;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;
import co.realtime.storage.ext.StorageException;

/**
 * The Class ActiveRecord.
 */
/* cannot be instantiated */
public abstract class ActiveRecord {

    /** The table name. */
    protected String tableName;

    /** The secondary key name. */
    protected String primaryKeyName;

    /** The secondary key name. */
    protected String secondaryKeyName;

    /** The table ref. */
    protected TableRef tableRef = null;

    /** The item ref. */
    private ItemRef itemRef = null;

    /**
     * Instantiates a new active record.
     */
    public ActiveRecord() {

        final Class<?> klass = this.getClass();

        // is an Entity
        this.tableName = klass.getSimpleName().replace("Record", "");
        if (klass.isAnnotationPresent(StorageTable.class)) {

            // retrieve table name
            final StorageTable storageTableAnnotation = klass.getAnnotation(StorageTable.class);
            final String name = storageTableAnnotation.name();
            if (name != null && !name.isEmpty()) {
                this.tableName = name;
            }

            // retrieve primary key name
            this.primaryKeyName = storageTableAnnotation.primaryKey();

            // retrieve secondary key name
            this.secondaryKeyName = storageTableAnnotation.secondaryKey();

        }

    }

    /**
     * Gets the primary key type.
     * @return the primary key type
     */
    public StorageDataType getPrimaryKeyType() {

        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field f : fields) {
            if (f.isAnnotationPresent(StorageProperty.class)) {
                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                if (storagePropertyAnnotation.isPrimaryKey()) {
                    try {
                        f.setAccessible(true);
                        final Object primaryKeyFieldInstance = f.getClass().newInstance();
                        if (primaryKeyFieldInstance instanceof Number) {
                            return StorageDataType.NUMBER;
                        }
                    } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                        // TODO
                    }
                }
            }
        }

        return StorageDataType.STRING;

    }

    /**
     * Gets the secondary key type.
     * @return the secondary key type
     */
    public StorageDataType getSecondaryKeyType() {

        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field f : fields) {
            if (f.isAnnotationPresent(StorageProperty.class)) {
                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                if (storagePropertyAnnotation.isSecondaryKey()) {
                    try {
                        f.setAccessible(true);
                        final Object secondaryKeyFieldInstance = f.getClass().newInstance();
                        if (secondaryKeyFieldInstance instanceof Number) {
                            return StorageDataType.NUMBER;
                        }
                    } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                        // TODO
                    }
                }
            }
        }

        return StorageDataType.STRING;

    }

    /**
     * Gets the primary key.
     * @return the primary key
     */
    public Object getPrimaryKey() {

        final Field[] fields = this.getClass().getDeclaredFields();
        Field fieldByPrimaryKeyName = null;
        for (final Field f : fields) {
            if (f.isAnnotationPresent(StorageProperty.class)) {
                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                if (storagePropertyAnnotation.isPrimaryKey()) {
                    try {
                        f.setAccessible(true);
                        return f.get(this);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (f.getName().equals(this.primaryKeyName)) {
                    fieldByPrimaryKeyName = f;
                }
            }
        }

        // default if no StorageProperty with isPrimaryKey attribute
        if (fieldByPrimaryKeyName != null) {
            try {
                fieldByPrimaryKeyName.setAccessible(true);
                return fieldByPrimaryKeyName.get(this);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;

    }

    /**
     * Gets the secondary key.
     * @return the secondary key
     */
    public Object getSecondaryKey() {

        final Field[] fields = this.getClass().getDeclaredFields();
        Field fieldBySecondaryKeyName = null;
        for (final Field f : fields) {
            if (f.isAnnotationPresent(StorageProperty.class)) {
                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                if (storagePropertyAnnotation.isSecondaryKey()) {
                    try {
                        f.setAccessible(true);
                        return f.get(this);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (f.getName().equals(this.secondaryKeyName)) {
                    fieldBySecondaryKeyName = f;
                }
            }
        }

        // default if no StorageProperty with isPrimaryKey attribute
        if (fieldBySecondaryKeyName != null) {
            try {
                fieldBySecondaryKeyName.setAccessible(true);
                return fieldBySecondaryKeyName.get(this);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;

    }

    /**
     * Map attributes from item snapshot.
     * @param itemSnapshot
     *            the item snapshot
     */
    public void mapAttributesFromItemSnapshot(final ItemSnapshot itemSnapshot) {

        if (itemSnapshot != null) {

            final HashMap<String, ItemAttribute> values = itemSnapshot.val();

            try {
                clearStorageInfo();
                this.itemRef = itemSnapshot.ref();
                mapAttributesToInstance(ActiveRecord.convertToKeyValueAttributes(values));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO
            }

        }

    }

    /**
     * Clear storage info.
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    public void clearStorageInfo() throws IllegalArgumentException, IllegalAccessException {
        this.itemRef = null;
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field f : fields) {
            if (f.isAnnotationPresent(StorageProperty.class)) {
                f.setAccessible(true);
                f.set(this, null);
            }
        }
    }

    /**
     * Fetch attributes to instance.
     * @param attributes
     *            the attributes
     */
    public void mapAttributesToInstance(final HashMap<String, Object> attributes) {

        // FIXME improve the code -- split it up and reuse more chunks of code
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field f : fields) {

            if (f.isAnnotationPresent(StorageProperty.class)) {

                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                String propertyName = f.getName();
                if (storagePropertyAnnotation.name() != null && !storagePropertyAnnotation.name().isEmpty()) {
                    propertyName = storagePropertyAnnotation.name();
                }
                try {
                    final Object attribute = attributes.get(propertyName);
                    if (attribute != null) {
                        f.setAccessible(true);
                        f.set(this, attribute);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {

                final StoragePropertyEnum storagePropertyEnumAnnotation = f.getAnnotation(StoragePropertyEnum.class);
                String propertyName = f.getName();
                if (storagePropertyEnumAnnotation.name() != null && !storagePropertyEnumAnnotation.name().isEmpty()) {
                    propertyName = storagePropertyEnumAnnotation.name();
                }

                try {
                    final Object attribute = attributes.get(propertyName);
                    if (attribute != null) {
                        f.setAccessible(true);
                        f.set(this, Enum.valueOf((Class<? extends Enum>) f.getType(), attribute.toString()));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {

                // FIXME no nest resources for now
                final JsonCollectionStorageProperty jsonCollectionStoragePropertyAnnotation = f.getAnnotation(JsonCollectionStorageProperty.class);
                String collectionName = f.getName();
                if (jsonCollectionStoragePropertyAnnotation.name() != null && !jsonCollectionStoragePropertyAnnotation.name().isEmpty()) {
                    collectionName = jsonCollectionStoragePropertyAnnotation.name();
                }

                f.setAccessible(true);
                final List items = new ArrayList<>();

                try {
                    final String jsonItemsAsString = (String) attributes.get(collectionName);
                    if (jsonItemsAsString != null && !jsonItemsAsString.isEmpty()) {
                        final JSONArray jsonItems = new JSONArray(jsonItemsAsString);
                        for (int i = 0; i < jsonItems.length(); i++) {
                            final JSONObject jsonItem = jsonItems.getJSONObject(i);
                            final Class<?> klass = jsonCollectionStoragePropertyAnnotation.klass();
                            final Object itemInstance = klass.newInstance();
                            final Field[] itemFields = klass.getDeclaredFields();
                            for (final Field itemField : itemFields) {

                                if (itemField.isAnnotationPresent(StorageProperty.class)) {
                                    final StorageProperty storagePropertyAnnotation = itemField.getAnnotation(StorageProperty.class);
                                    String propertyName = itemField.getName();
                                    if (storagePropertyAnnotation.name() != null && !storagePropertyAnnotation.name().isEmpty()) {
                                        propertyName = storagePropertyAnnotation.name();
                                    }
                                    try {

                                        if (jsonItem.has(propertyName)) {

                                            final Object attribute = jsonItem.get(propertyName);
                                            if (attribute != null) {
                                                itemField.setAccessible(true);
                                                itemField.set(itemInstance, attribute);
                                            }

                                        }
                                    } catch (IllegalArgumentException | IllegalAccessException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }

                            }
                            items.add(itemInstance);
                        }
                    }
                    f.set(this, items);
                } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * Attributes.
     * @return the hash map
     */
    public HashMap<String, Object> attributes() {

        // FIXME improve this code to reuse duplicated code and be more clean

        final HashMap<String, Object> attributes = new LinkedHashMap<>(0);
        final Field[] fields = this.getClass().getDeclaredFields();

        for (final Field f : fields) {

            if (f.isAnnotationPresent(StorageProperty.class)) {
                final StorageProperty storagePropertyAnnotation = f.getAnnotation(StorageProperty.class);
                String propertyName = f.getName();
                if (storagePropertyAnnotation.name() != null && !storagePropertyAnnotation.name().isEmpty()) {
                    propertyName = storagePropertyAnnotation.name();
                }
                try {

                    f.setAccessible(true);
                    final Object fieldValue = f.get(this);
                    if (fieldValue != null) {
                        attributes.put(propertyName, fieldValue);
                    }

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (f.isAnnotationPresent(StoragePropertyEnum.class)) {

                final StoragePropertyEnum storagePropertyEnum = f.getAnnotation(StoragePropertyEnum.class);
                String propertyName = f.getName();
                if (storagePropertyEnum.name() != null && !storagePropertyEnum.name().isEmpty()) {
                    propertyName = storagePropertyEnum.name();
                }

                try {

                    f.setAccessible(true);
                    final Object fieldValue = f.get(this);
                    if (fieldValue != null) {
                        attributes.put(propertyName, fieldValue.toString());
                    }

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if (f.isAnnotationPresent(JsonCollectionStorageProperty.class)) {

                final JsonCollectionStorageProperty jsonCollectionStorageProperty = f.getAnnotation(JsonCollectionStorageProperty.class);
                String propertyName = f.getName();
                if (jsonCollectionStorageProperty.name() != null && !jsonCollectionStorageProperty.name().isEmpty()) {
                    propertyName = jsonCollectionStorageProperty.name();
                }

                try {

                    f.setAccessible(true);
                    final Collection<?> items = (Collection<?>) f.get(this);

                    if (items != null) {

                        final JSONArray array = new JSONArray();
                        for (final Object item : items) {

                            final JSONObject jsonItem = new JSONObject();
                            final Field[] itemFields = item.getClass().getDeclaredFields();
                            for (final Field itemField : itemFields) {

                                if (itemField.isAnnotationPresent(StorageProperty.class)) {
                                    final StorageProperty storagePropertyAnnotation = itemField.getAnnotation(StorageProperty.class);
                                    String fieldPropertyName = itemField.getName();
                                    if (storagePropertyAnnotation.name() != null && !storagePropertyAnnotation.name().isEmpty()) {
                                        fieldPropertyName = storagePropertyAnnotation.name();
                                    }

                                    try {

                                        itemField.setAccessible(true);
                                        final Object fieldValue = itemField.get(item);
                                        if (fieldValue != null) {
                                            jsonItem.put(fieldPropertyName, fieldValue);
                                        }

                                    } catch (IllegalArgumentException | IllegalAccessException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }

                            }

                            array.put(jsonItem);

                        }

                        attributes.put(propertyName, array.toString());

                    }

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        return attributes;

    }

    /**
     * Convert to item attributes.
     * @param keyValueAttributes
     *            the key value attributes
     * @return the hash map
     */
    private static HashMap<String, ItemAttribute> convertToItemAttributes(final HashMap<String, Object> keyValueAttributes) {

        final HashMap<String, ItemAttribute> attributes = new LinkedHashMap<>(0);
        if (keyValueAttributes != null && !keyValueAttributes.isEmpty()) {
            for (final String key : keyValueAttributes.keySet()) {
                final Object value = keyValueAttributes.get(key);
                if (value != null) {
                    if (value instanceof String) {
                        attributes.put(key, new ItemAttribute((String) value));
                    } else if (value instanceof Number) {
                        attributes.put(key, new ItemAttribute((Number) value));
                    }
                }
            }
        }

        return attributes;

    }

    /**
     * Convert to key value attributes.
     * @param attributes
     *            the attributes
     * @return the hash map
     */
    static HashMap<String, Object> convertToKeyValueAttributes(final HashMap<String, ItemAttribute> attributes) {

        final HashMap<String, Object> keyValueAttributes = new LinkedHashMap<>(0);
        if (attributes != null && !attributes.isEmpty()) {
            for (final String key : attributes.keySet()) {
                final ItemAttribute attribute = attributes.get(key);
                keyValueAttributes.put(key, attribute.get());
            }
        }

        return keyValueAttributes;

    }

    /**
     * Creates the query.
     * @param concreteActiveRecordClass
     *            the concrete active record class
     * @return the table ref
     * @throws StorageException
     *             the storage exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    public static QueryRef<? extends ActiveRecord> createQuery(final Class<? extends ActiveRecord> concreteActiveRecordClass) throws StorageException, InstantiationException, IllegalAccessException {
        return new QueryRef<>(concreteActiveRecordClass);
    }

    /**
     * Fetch all.
     * @param concreteActiveRecordClass
     *            the concrete active record class
     * @param onSuccess
     *            the on success
     * @param onError
     *            the on error
     * @return the list
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws StorageException
     *             the storage exception
     */
    public static ActiveRecordsCollectionStateFuture<? extends ActiveRecord> fetchAll(final Class<? extends ActiveRecord> concreteActiveRecordClass, final OnSuccessCollectionCommand<ActiveRecord> onSuccess, final OnErrorCommand onError) throws InstantiationException, IllegalAccessException, StorageException {
        return executeQuery(new QueryRef<>(concreteActiveRecordClass), onSuccess, onError);
    }

    /**
     * Execute query.
     * @param query
     *            the query
     * @param onSuccess
     *            the on success
     * @param onError
     *            the on error
     * @return the active records collection state future
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws StorageException
     *             the storage exception
     */
    public static ActiveRecordsCollectionStateFuture<? extends ActiveRecord> executeQuery(final QueryRef<? extends ActiveRecord> query, final OnSuccessCollectionCommand<ActiveRecord> onSuccess, final OnErrorCommand onError) throws InstantiationException, IllegalAccessException, StorageException {

        if (query == null) {
            throw new IllegalArgumentException("query argument can not be null");
        }

        return query.getResults(onSuccess, onError);

    }

    /**
     * Fetch.
     * @param successCallback
     *            the success callback
     * @param errorCallback
     *            the error callback
     * @return the active record state future
     * @throws StorageException
     *             the storage exception
     */
    public ActiveRecordStateFuture<? extends ActiveRecord> fetch(final OnSuccessRecordCommand<? extends ActiveRecord> successCallback, final OnErrorCommand errorCallback) throws StorageException {

        final ActiveRecordStateFuture<ActiveRecord> future = new ActiveRecordStateFuture(successCallback, errorCallback);

        final Object primaryKey = getPrimaryKey();
        final Object secondaryKey = getSecondaryKey();

        if (primaryKey == null) {
            throw new IllegalAccessError("Cannot access record because doesnt have primary key defined!");
        }

        flushTableRef();
        ItemAttribute primaryKeyAttribute = null;
        ItemAttribute secondaryKeyAttribute = null;

        // primary key attribute
        if (primaryKey instanceof String) {
            primaryKeyAttribute = new ItemAttribute((String) primaryKey);
        } else if (primaryKey instanceof Number) {
            primaryKeyAttribute = new ItemAttribute((Number) primaryKey);
        }

        // secondary key attribute
        if (secondaryKey != null) {
            if (secondaryKey instanceof String) {
                secondaryKeyAttribute = new ItemAttribute((String) secondaryKey);
            } else if (secondaryKey instanceof Number) {
                secondaryKeyAttribute = new ItemAttribute((Number) secondaryKey);
            }
        }

        final ItemRef itemRefTemp = this.tableRef.item(primaryKeyAttribute, secondaryKeyAttribute);
        final ActiveRecord weakReference = this;

        itemRefTemp.get(new OnItemSnapshot() {

            @Override
            public void run(final ItemSnapshot itemSnapshot) {

                if (itemSnapshot != null && !itemSnapshot.val().isEmpty()) {
                    try {
                        weakReference.mapAttributesFromItemSnapshot(itemSnapshot);
                        future.processRecordAsync(weakReference);
                    } catch (final InterruptedException e) {
                        // TODO
                    }
                } else {
                    try {
                        future.processError(new Error(Integer.valueOf(404), "No Item"));
                    } catch (final InterruptedException e) {
                        // TODO
                    }
                }

            }

        }, new OnError() {

            @Override
            public void run(final Integer errorCode, final String errorMessage) {

                try {
                    future.processError(new Error(errorCode, errorMessage));
                } catch (final InterruptedException e) {
                    // TODO
                }

            }

        });

        return future;

    }

    /**
     * Save.
     * @param successCallback
     *            the success callback
     * @param errorCallback
     *            the error callback
     * @return the active record state future
     * @throws StorageException
     *             the storage exception
     */
    public ActiveRecordStateFuture<? extends ActiveRecord> save(final OnSuccessRecordCommand<? extends ActiveRecord> successCallback, final OnErrorCommand errorCallback) throws StorageException {

        final ActiveRecordStateFuture<ActiveRecord> future = new ActiveRecordStateFuture(successCallback, errorCallback);

        flushTableRef();
        final ActiveRecord weakReference = this;

        this.tableRef.push((LinkedHashMap<String, ItemAttribute>) convertToItemAttributes(attributes()), new OnItemSnapshot() {

            @Override
            public void run(final ItemSnapshot itemSnapshot) {

                try {
                    weakReference.mapAttributesFromItemSnapshot(itemSnapshot);
                    future.processRecordAsync(weakReference);
                } catch (final InterruptedException e) {
                    // TODO
                }

            }

        }, new OnError() {

            @Override
            public void run(final Integer errorCode, final String errorMessage) {

                try {
                    future.processError(new Error(errorCode, errorMessage));
                } catch (final InterruptedException e) {
                    // TODO
                }
            }

        });

        return future;

    }

    /**
     * Delete.
     * @param successCallback
     *            the success callback
     * @param errorCallback
     *            the error callback
     * @return the active record state future
     * @throws StorageException
     *             the storage exception
     */
    public ActiveRecordStateFuture<? extends ActiveRecord> delete(final OnSuccessRecordCommand<? extends ActiveRecord> successCallback, final OnErrorCommand errorCallback) throws StorageException {

        final ActiveRecordStateFuture<ActiveRecord> future = new ActiveRecordStateFuture(successCallback, errorCallback);

        if (isFromStorage()) {

            final ActiveRecord weakReference = this;
            this.itemRef.del(new OnItemSnapshot() {

                @Override
                public void run(final ItemSnapshot itemSnapshot) {
                    weakReference.mapAttributesFromItemSnapshot(itemSnapshot);
                    try {
                        future.processRecordAsync(weakReference);
                    } catch (final InterruptedException e) {
                        // TODO
                    }
                }

            }, new OnError() {

                @Override
                public void run(final Integer errorCode, final String errorMessage) {
                    try {
                        future.processError(new Error(errorCode, errorMessage));
                    } catch (final InterruptedException e) {
                        // TODO
                    }
                }

            });

        } else {

            fetch(new OnSuccessRecordCommand<ActiveRecord>() {

                @Override
                public void execute(final ActiveRecord record) {
                    try {
                        record.delete(successCallback, errorCallback);
                    } catch (final StorageException storageException) {
                        // TODO
                    }
                }

            }, null);

        }

        return future;

    }

    /**
     * Flush table ref.
     * @return the table ref
     * @throws StorageException
     *             the storage exception
     */
    protected TableRef flushTableRef() throws StorageException {
        final StorageRef storageRef = StorageRefFactorySingleton.INSTANCE.getStorageRef();
        this.tableRef = storageRef.table(this.tableName);
        return this.tableRef;

    }

    /**
     * Gets the table name.
     * @return the table name
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Gets the primary key name.
     * @return the primary key name
     */
    public String getPrimaryKeyName() {
        return this.primaryKeyName;
    }

    /**
     * Sets the primary key name.
     * @param primaryKeyName
     *            the new primary key name
     */
    public void setPrimaryKeyName(final String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    /**
     * Gets the secondary key name.
     * @return the secondary key name
     */
    public String getSecondaryKeyName() {
        return this.secondaryKeyName;
    }

    /**
     * Sets the secondary key name.
     * @param secondaryKeyName
     *            the new secondary key name
     */
    public void setSecondaryKeyName(final String secondaryKeyName) {
        this.secondaryKeyName = secondaryKeyName;
    }

    /**
     * Gets the table ref.
     * @return the table ref
     * @throws StorageException
     *             the storage exception
     */
    public TableRef getTableRef() throws StorageException {
        flushTableRef();
        return this.tableRef;
    }

    /**
     * Sets the table ref.
     * @param tableRef
     *            the new table ref
     */
    public void setTableRef(final TableRef tableRef) {
        this.tableRef = tableRef;
    }

    /**
     * Checks if is new record.
     * @return true, if is new record
     */
    public boolean isFromStorage() {
        return this.itemRef != null;
    }

}
