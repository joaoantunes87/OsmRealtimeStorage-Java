package co.realtime.storage.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import co.realtime.storage.StorageRef;
import co.realtime.storage.ext.StorageException;

/**
 * The Enum StorageRefFactorySingleton.
 */
public enum StorageRefFactorySingleton {

    /** The instance. */
    INSTANCE;

    /** The Constant CONFIGURATIONS_PATH. */
    private static final String CONFIGURATIONS_PATH = "META-INF/storage.properties";

    /** The Constant APPLICATION_KEY_PROPERTY. */
    private static final String APPLICATION_KEY_PROPERTY = "storage.application_key";

    /** The Constant PRIVATE_KEY_PROPERTY. */
    private static final String PRIVATE_KEY_PROPERTY = "storage.private_key";

    /** The Constant AUTHENTICATION_TOKEN_PROPERTY. */
    private static final String AUTHENTICATION_TOKEN_PROPERTY = "storage.authentication_token";

    /** The Constant CLUSTER_PROPERTY. */
    private static final String CLUSTER_PROPERTY = "storage.cluster";

    /** The Constant SECURE_PROPERTY. */
    private static final String SECURE_PROPERTY = "storage.secure";

    /** The Constant ENDPOINT_PROPERTY. */
    private static final String ENDPOINT_PROPERTY = "storage.endpoint";

    // storage connection properties
    /** The application key. */
    private String applicationKey;

    /** The private key. */
    private String privateKey;

    /** The authentication token. */
    private String authenticationToken;

    /** The cluster. */
    private boolean cluster;

    /** The secure. */
    private boolean secure;

    /** The endpoint. */
    private String endpoint;

    /** The configuration path. */
    private String configurationPath;

    // reference to storage
    /** The storage ref. */
    private StorageRef storageRef = null;

    /**
     * Instantiates a new storage ref factory singleton.
     */
    private StorageRefFactorySingleton() {
        this.configurationPath = CONFIGURATIONS_PATH;
        loadStorageProperties();
    }

    /**
     * Reload configurations.
     * @param configurationPath
     *            the configuration path
     */
    public void reloadConfigurations(final String configurationPath) {

        if (configurationPath != null && !configurationPath.isEmpty()) {
            this.configurationPath = configurationPath;
            loadStorageProperties();
        }

    }

    /**
     * Load storage properties.
     */
    private void loadStorageProperties() {

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = cl.getResourceAsStream(this.configurationPath);) {

            if (is != null) {

                final Properties properties = new Properties();
                properties.load(is);

                if (properties.containsKey(APPLICATION_KEY_PROPERTY)) {
                    this.applicationKey = properties.getProperty(APPLICATION_KEY_PROPERTY);
                }

                if (properties.containsKey(PRIVATE_KEY_PROPERTY)) {
                    this.privateKey = properties.getProperty(PRIVATE_KEY_PROPERTY);
                }

                if (properties.containsKey(AUTHENTICATION_TOKEN_PROPERTY)) {
                    this.authenticationToken = properties.getProperty(AUTHENTICATION_TOKEN_PROPERTY);
                }

                if (properties.containsKey(CLUSTER_PROPERTY)) {
                    this.cluster = Boolean.valueOf(properties.getProperty(CLUSTER_PROPERTY)).booleanValue();
                }

                if (properties.containsKey(SECURE_PROPERTY)) {
                    this.secure = Boolean.valueOf(properties.getProperty(SECURE_PROPERTY)).booleanValue();
                }

                if (properties.containsKey(ENDPOINT_PROPERTY)) {
                    this.endpoint = properties.getProperty(ENDPOINT_PROPERTY);
                }

            }

        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Gets the storage ref.
     * @return the storage ref
     * @throws StorageException
     *             the storage exception
     */
    public StorageRef getStorageRef() throws StorageException {

        if (this.storageRef == null) {
            this.storageRef = new StorageRef(this.applicationKey, this.privateKey, this.authenticationToken, this.cluster, this.secure, this.endpoint);
        }

        return this.storageRef;

    }

}
