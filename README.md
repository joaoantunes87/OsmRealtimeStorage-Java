<h1>Introduction</h1>

The idea of this project is being an automatic mapper for Realtime Cloud Storage. Mapping automatically the data retrieved from the Storage to Plain Old Java Objects ( POJO ) with minimum effort. You only need to define a POJO per Table, which must extends the class ActiveRecord, and configure it using the provided Annotations. Once this is done your POJO will gain the following features:

<ul>
  <li>fetch - retrieve the item from primaryKey and secondaryKey</li>
  <li>delete - delete the item</li>
  <li>save - update or create the item</li>
  <li>Query Features</li>
</ul>

<h1>Concepts</h1>

<h2>ActiveRecord</h2>

<h2>NoSQL</h2>

<h2>Realtime Cloud Storage</h2>

<h2>Annotations</h2>

<h1>How to Use</h1>

<h2>Maven</h2>

<h2>Configurations</h2>

storage.properties example

<pre>
storage.application_key = your_application_key                            # mandatory
storage.cluster = true                                                    # mandatory
storage.endpoint = https://storage-balancer.realtime.co/server/ssl/1.0    # mandatory
storage.secure = true                                                     # optional
storage.private_key = your_private_key                                    # optional
storage.authentication_token = your_auth_token                            # if secure true
</pre>

<h2>J2EE Integration</h2>

In your web.xml add the following

```xml
    <servlet>
        <servlet-name>StorageInitializerServlet</servlet-name>
        <servlet-class>co.realtime.storage.servlets.StorageInitializerServlet</servlet-class>
        <init-param>
            <param-name>storage.configurations.path</param-name>
            <param-value>/META-INF/realtime/storage.properties</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet> 
```
</pre>

<h2>POJO Example</h2> 

```java

package co.realtime.storage.models;

import co.realtime.storage.annotations.StorageProperty;
import co.realtime.storage.annotations.StorageTable;

/**
 * The Class EntityRecord.
 */
@StorageTable(name = "Entity", primaryKey = "cnes", secondaryKey = "cap")
public class EntityRecordSample extends ActiveRecord {

    /** The cnes. */
    @StorageProperty(name = "cnes", isPrimaryKey = true)
    private String cnes;

    /** The name. */
    @StorageProperty(name = "name")
    private String name;

    /** The endpoint. */
    @StorageProperty(name = "endpoint")
    private String endpoint;

    /** The cap. */
    @StorageProperty(name = "cap", isSecondaryKey = true)
    private String cap;

    /**
     * Instantiates a new entity record.
     */
    public EntityRecordSample() {
        super();
    }

}


```

<h2>Api</h2>

Check out the Unit tests at src/test/java/co/realtime/storage for mor details

<h3>CRUD Example</h3>

```java

    @Test
    public void aFullCrudTest() throws StorageException, InterruptedException, ExecutionException {

        final String cnes = "1";
        final String cap = "C1";

        ActiveRecordStateFuture<EntityRecordSample> future = null;

        // create entity
        final EntityRecordSample newEntity = new EntityRecordSample();

        newEntity.setCnes(cnes);
        newEntity.setCap(cap);
        newEntity.setName("Rio");

        future = (ActiveRecordStateFuture<EntityRecordSample>) newEntity.save(null, null);
        future.get();

        // fetch created entity
        final EntityRecordSample fetchedEntity = new EntityRecordSample();
        fetchedEntity.setCnes(cnes);
        fetchedEntity.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) fetchedEntity.fetch(null, null);
        future.get();
        assertEquals("Rio", fetchedEntity.getName());

        // update entity
        fetchedEntity.setName("Rio Updated");
        future = (ActiveRecordStateFuture<EntityRecordSample>) fetchedEntity.save(null, null);
        future.get();

        final EntityRecordSample updatedEntity = new EntityRecordSample();
        updatedEntity.setCnes(cnes);
        updatedEntity.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) updatedEntity.fetch(null, null);
        future.get();
        assertEquals("Rio Updated", updatedEntity.getName());

        // delete entity
        future = (ActiveRecordStateFuture<EntityRecordSample>) updatedEntity.delete(null, null);
        future.get();

        final EntityRecordSample afterDelete = new EntityRecordSample();
        afterDelete.setCnes(cnes);
        afterDelete.setCap(cap);

        future = (ActiveRecordStateFuture<EntityRecordSample>) afterDelete.fetch(null, null);

        final ActiveRecordState<EntityRecordSample> state = future.get();
        assertEquals(Boolean.TRUE, Boolean.valueOf(state.hasError()));

    }
    
```

<h3>Query Example</h3>

```java

    @Test
    public void fetchAllRioCapEntities() throws InstantiationException, IllegalAccessException, StorageException, InterruptedException, ExecutionException {
        final QueryRef<EntityRecordSample> query = (QueryRef<EntityRecordSample>) EntityRecordSample.createQuery(EntityRecordSample.class);
        query.ref().equals("cap", new ItemAttribute("rio"));
        final ActiveRecordsCollectionStateFuture<EntityRecordSample> future = (ActiveRecordsCollectionStateFuture<EntityRecordSample>) EntityRecordSample.executeQuery(query, null, null);
        final ActiveRecordsCollectionState<EntityRecordSample> collectionState = future.get();
        final List<EntityRecordSample> records = collectionState.records();
        assertEquals(1, records.size());

    }
    
```


<h1>Roadmap</h1>
