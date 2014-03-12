package co.realtime.storage.api;

import co.realtime.storage.models.ActiveRecord;

/**
 * The Class ActiveRecordState.
 * @param <R>
 *            the generic type
 */
public class ActiveRecordState<R extends ActiveRecord> {

    /** The record. */
    private final R record;

    /** The error. */
    private final Error error;

    /**
     * Instantiates a new active record state.
     * @param record
     *            the record
     */
    public ActiveRecordState(final R record) {
        this.record = record;
        this.error = null;
    }

    /**
     * Instantiates a new active record state.
     * @param error
     *            the error
     */
    public ActiveRecordState(final Error error) {
        this.record = null;
        this.error = error;
    }

    /**
     * Record.
     * @return the r
     */
    public R record() {
        return this.record;
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
