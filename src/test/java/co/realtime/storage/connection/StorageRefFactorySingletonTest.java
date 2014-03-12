package co.realtime.storage.connection;

import org.junit.Test;

import co.realtime.storage.connection.StorageRefFactorySingleton;
import co.realtime.storage.ext.StorageException;

public class StorageRefFactorySingletonTest {

    @Test
    public void testStorageConnection() throws StorageException {
        StorageRefFactorySingleton.INSTANCE.getStorageRef();
    }

}
