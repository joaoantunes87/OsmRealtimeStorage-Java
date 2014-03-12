package co.realtime.storage.api;

import java.util.ArrayList;
import java.util.List;

import co.realtime.storage.models.ActiveRecord;

/**
 * The Class ActiveRecordsCollectionState.
 * @param <R>
 *            the generic type
 */
public class ActiveRecordsCollectionState<R extends ActiveRecord> {

    /** The records. */
    private final List<R> records;

    /** The error. */
    private final Error error;

    /**
     * Instantiates a new active records collection state.
     * @param records
     *            the records
     */
    public ActiveRecordsCollectionState(final List<R> records) {
        this.records = records;
        this.error = null;
    }

    /**
     * Instantiates a new active records collection state.
     * @param error
     *            the error
     */
    public ActiveRecordsCollectionState(final Error error) {
        this.records = new ArrayList<>(0);
        this.error = error;
    }

    /**
     * Records.
     * @return the list
     */
    public List<R> records() {
        return this.records;
    }

    /**
     * Error.
     * @return the error
     */
    public Error error() {
        return this.error;
    }

    /**
     * Checks for error.
     * @return true, if successful
     */
    public boolean hasError() {
        return this.error != null;
    }

}
