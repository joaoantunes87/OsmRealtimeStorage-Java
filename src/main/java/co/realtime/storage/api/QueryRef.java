package co.realtime.storage.api;

import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.TableRef;
import co.realtime.storage.async.ActiveRecordsCollectionStateFuture;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;
import co.realtime.storage.ext.StorageException;
import co.realtime.storage.models.ActiveRecord;

/**
 * The Class QueryRef.
 * @param <R>
 *            the generic type
 */
public class QueryRef<R extends ActiveRecord> {

    /** The ref. */
    private final TableRef ref;

    /** The klass. */
    private final Class<R> klass;

    /**
     * Instantiates a new query ref.
     * @param concreteActiveRecordClass
     *            the concrete active record class
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws StorageException
     *             the storage exception
     */
    public QueryRef(final Class<R> concreteActiveRecordClass) throws InstantiationException, IllegalAccessException, StorageException {
        final R instance = concreteActiveRecordClass.newInstance();
        this.klass = (Class<R>) instance.getClass();
        this.ref = instance.getTableRef();
    }

    /**
     * Ref.
     * @return the table ref
     */
    public TableRef ref() {
        return this.ref;
    }

    /**
     * Gets the results.
     * @param onSuccess
     *            the on success
     * @param onError
     *            the on error
     * @return the results
     */
    public ActiveRecordsCollectionStateFuture<R> getResults(final OnSuccessCollectionCommand<ActiveRecord> onSuccess, final OnErrorCommand onError) {

        final ActiveRecordsCollectionStateFuture<R> future = (ActiveRecordsCollectionStateFuture<R>) new ActiveRecordsCollectionStateFuture<>(onSuccess, onError);
        final Class<R> concreteRecordClass = this.klass;

        this.ref.getItems(new OnItemSnapshot() {

            @Override
            public void run(final ItemSnapshot itemSnapshot) {

                // all records have been sent -- finalizing process
                if (itemSnapshot == null) {

                    try {
                        future.processRecordsAsync();
                    } catch (final InterruptedException e) {
                        // TODO
                    }

                } else {

                    try {
                        final R record = concreteRecordClass.newInstance();
                        record.mapAttributesFromItemSnapshot(itemSnapshot);
                        future.addRecord(record);
                    } catch (InstantiationException | IllegalAccessException e) {
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
}
