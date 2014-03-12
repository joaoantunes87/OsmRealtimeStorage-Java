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

    /** The provider name. */
    @StorageProperty(name = "providerName")
    private String providerName;

    /** The username. */
    @StorageProperty(name = "username")
    private String username;

    /** The password. */
    @StorageProperty(name = "password")
    private String password;

    /** The system. */
    @StorageProperty(name = "system")
    private String system;

    /**
     * Instantiates a new entity record.
     */
    public EntityRecordSample() {
        super();
    }

    /**
     * Gets the cnes.
     * @return the cnes
     */
    public String getCnes() {
        return this.cnes;
    }

    /**
     * Sets the cnes.
     * @param cnes
     *            the new cnes
     */
    public void setCnes(final String cnes) {
        this.cnes = cnes;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the endpoint.
     * @return the endpoint
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Sets the endpoint.
     * @param endpoint
     *            the new endpoint
     */
    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the cap.
     * @return the cap
     */
    public String getCap() {
        return this.cap;
    }

    /**
     * Sets the area cap.
     * @param cap
     *            the new cap
     */
    public void setCap(final String cap) {
        this.cap = cap;
    }

    /**
     * Gets the provider name.
     * @return the provider name
     */
    public String getProviderName() {
        return this.providerName;
    }

    /**
     * Sets the provider name.
     * @param providerName
     *            the new provider name
     */
    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    /**
     * Gets the username.
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username.
     * @param username
     *            the new username
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password.
     * @param password
     *            the new password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Gets the system.
     * @return the system
     */
    public String getSystem() {
        return this.system;
    }

    /**
     * Sets the system.
     * @param system
     *            the new system
     */
    public void setSystem(final String system) {
        this.system = system;
    }

}
