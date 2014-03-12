package co.realtime.storage.models;

import co.realtime.storage.annotations.StorageProperty;

/**
 * The Class ConnectionInfo.
 */
public class ConnectionInfoSample {

    /** The name. */
    @StorageProperty(name = "name")
    private String name;

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

}
