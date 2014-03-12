package co.realtime.storage.api;

import co.realtime.storage.models.ActiveRecord;

/**
 * The Class OnSuccessRecordCallback.
 * @param <I>
 *            the generic type
 */
public abstract class OnSuccessRecordCommand<I extends ActiveRecord> {

    /**
     * Execute.
     * @param weakReference
     *            the record
     */
    public abstract void execute(ActiveRecord record);
}
