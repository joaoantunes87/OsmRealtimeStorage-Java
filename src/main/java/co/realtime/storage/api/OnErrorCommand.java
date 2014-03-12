package co.realtime.storage.api;

/**
 * The Class OnErrorCallback.
 */
public abstract class OnErrorCommand {

    /**
     * Execute.
     * @param error
     *            the error
     */
    public abstract void execute(Error error);
}
