package co.realtime.storage.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.realtime.storage.api.ActiveRecordState;
import co.realtime.storage.api.Error;
import co.realtime.storage.api.OnErrorCommand;
import co.realtime.storage.api.OnSuccessRecordCommand;
import co.realtime.storage.models.ActiveRecord;

/**
 * The Class OnSuccessRecordCallable.
 * @param <I>
 *            the generic type
 */
public class ActiveRecordStateFuture<R extends ActiveRecord> implements Future<ActiveRecordState<R>> {

    /**
     * The Enum State.
     */
    private static enum State {

        /** The running. */
        RUNNING,
        /** The done. */
        DONE,
        /** The cancelled. */
        CANCELLED
    }

    /** The reply. */
    private final BlockingQueue<ActiveRecordState<R>> reply = new ArrayBlockingQueue<>(1);

    /** The state. */
    private State state = State.RUNNING;

    /** The on success record command. */
    private final OnSuccessRecordCommand<R> onSuccessRecordCommand;

    /** The on error command. */
    private final OnErrorCommand onErrorCommand;

    /**
     * Instantiates a new on success record callable.
     * @param onSuccessRecordCommand
     *            the on success record command
     * @param onErrorCommand
     *            the on error command
     */
    public ActiveRecordStateFuture(final OnSuccessRecordCommand<R> onSuccessRecordCommand, final OnErrorCommand onErrorCommand) {
        this.onSuccessRecordCommand = onSuccessRecordCommand;
        this.onErrorCommand = onErrorCommand;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {

        if (this.isRunning()) {
            this.clearUp();
        }

        return this.isCancelled();

    }

    /**
     * Checks if is running.
     * @return true, if is running
     */
    public boolean isRunning() {
        return this.state == State.RUNNING;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return this.state == State.CANCELLED;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        return this.state == State.DONE;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public ActiveRecordState<R> get() throws InterruptedException, ExecutionException {
        return this.reply.take();

    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public ActiveRecordState<R> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        final ActiveRecordState<R> replyOrNull = this.reply.poll(timeout, unit);
        if (replyOrNull == null) {
            throw new TimeoutException();
        }

        return replyOrNull;

    }

    /**
     * Put record.
     * @param record
     *            the record
     * @throws InterruptedException
     *             the interrupted exception
     */
    public synchronized void processRecordAsync(final R record) throws InterruptedException {

        if (this.onSuccessRecordCommand != null) {
            this.onSuccessRecordCommand.execute(record);
        }

        this.reply.put(new ActiveRecordState<>(record));
        this.state = State.DONE;

    }

    /**
     * Process error.
     * @param error
     *            the error
     * @throws InterruptedException
     */
    public synchronized void processError(final Error error) throws InterruptedException {

        if (this.onErrorCommand != null) {
            this.onErrorCommand.execute(error);
        }

        this.reply.put(new ActiveRecordState<R>(error));
        this.state = State.DONE;

    }

    /**
     * Clear up.
     */
    private void clearUp() {
        this.reply.clear();
    }

}
