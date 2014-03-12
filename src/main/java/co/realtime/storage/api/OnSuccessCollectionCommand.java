package co.realtime.storage.api;

import java.util.List;

import co.realtime.storage.models.ActiveRecord;

/**
 * The Class OnSuccessCallback.
 * @param <I>
 *            the generic type
 */
public abstract class OnSuccessCollectionCommand<I extends ActiveRecord> {

    /**
     * Execute.
     * @param records
     *            the records
     */
    public abstract void execute(List<I> records);

}
