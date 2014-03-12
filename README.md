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
        <servlet-class>co.reatime.storage.servlets.StorageInitializerServlet</servlet-class>
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

Check out the Unit tests at src/test/java/co/realtime/storage
