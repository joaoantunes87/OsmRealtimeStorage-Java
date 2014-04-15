package co.realtime.storage.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.realtime.storage.api.ActiveRecordsCollectionState;
import co.realtime.storage.api.OnErrorCommand;
import co.realtime.storage.api.OnSuccessCollectionCommand;
import co.realtime.storage.exceptions.Error;
import co.realtime.storage.models.ActiveRecord;

/**
 * The Class ActiveRecordsCollectionStateFuture.
 * @param <R>
 *            the generic type
 */
public class ActiveRecordsCollectionStateFuture<R extends ActiveRecord> implements Future<ActiveRecordsCollectionState<R>> {

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
    private final BlockingQueue<ActiveRecordsCollectionState<R>> reply = new ArrayBlockingQueue<>(1);

    /** The records temp. */
    private final List<R> recordsTemp = new ArrayList<>();

    /** The state. */
    private State state = State.RUNNING;

    /** The on success collection command. */
    private final OnSuccessCollectionCommand<R> onSuccessCollectionCommand;

    /** The on error command. */
    private final OnErrorCommand onErrorCommand;

    /**
     * Instantiates a new active records collection state future.
     * @param onSuccessCollectionCommand
     *            the on success collection command
     * @param onErrorCommand
     *            the on error command
     */
    public ActiveRecordsCollectionStateFuture(final OnSuccessCollectionCommand<R> onSuccessCollectionCommand, final OnErrorCommand onErrorCommand) {
        this.onSuccessCollectionCommand = onSuccessCollectionCommand;
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
    public ActiveRecordsCollectionState<R> get() throws InterruptedException, ExecutionException {
        return this.reply.take();
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public ActiveRecordsCollectionState<R> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        final ActiveRecordsCollectionState<R> replyOrNull = this.reply.poll(timeout, unit);
        if (replyOrNull == null) {
            throw new TimeoutException();
        }

        return replyOrNull;

    }

    /**
     * Adds the record.
     * @param record
     *            the record
     */
    public synchronized void addRecord(final R record) {

        if (record != null) {
            this.recordsTemp.add(record);
        }

    }

    /**
     * Put record.
     * @throws InterruptedException
     *             the interrupted exception
     */
    public synchronized void processRecordsAsync() throws InterruptedException {

        if (this.onSuccessCollectionCommand != null) {
            this.onSuccessCollectionCommand.execute(this.recordsTemp);
        }

        this.reply.put(new ActiveRecordsCollectionState<>(this.recordsTemp));
        this.state = State.DONE;

    }

    /**
     * Process error.
     * @param error
     *            the error
     * @throws InterruptedException
     *             the interrupted exception
     */
    public synchronized void processError(final Error error) throws InterruptedException {

        if (this.onErrorCommand != null) {
            this.onErrorCommand.execute(error);
        }

        this.reply.put(new ActiveRecordsCollectionState<R>(error));
        this.state = State.DONE;

    }

    /**
     * Clear up.
     */
    private void clearUp() {
        this.reply.clear();
    }

}
