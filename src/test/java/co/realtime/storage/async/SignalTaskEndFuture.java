package co.realtime.storage.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Class SignalTaskEndFuture.
 */
public class SignalTaskEndFuture implements Future<Boolean> {

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
    private final BlockingQueue<Boolean> reply = new ArrayBlockingQueue<>(1);

    /** The state. */
    private final State state = State.RUNNING;

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {

        if (isRunning()) {
            clearUp();
        }

        return isCancelled();

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
    public Boolean get() throws InterruptedException, ExecutionException {

        if (!isRunning()) {
            throw new IllegalStateException("Task already finished");
        }

        return this.reply.take();

    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public Boolean get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        if (!isRunning()) {
            throw new IllegalStateException("Task already finished");
        }

        final Boolean replyOrNull = this.reply.poll(timeout, unit);
        if (replyOrNull == null) {
            throw new TimeoutException();
        }

        return replyOrNull;

    }

    /**
     * Clear up.
     */
    private void clearUp() {
        this.reply.clear();
    }

    /**
     * Mark success.
     * @throws InterruptedException
     *             the interrupted exception
     */
    public synchronized void markSuccess() throws InterruptedException {
        this.reply.put(Boolean.TRUE);
    }

    /**
     * Mark fail.
     * @throws InterruptedException
     *             the interrupted exception
     */
    public synchronized void markFail() throws InterruptedException {
        this.reply.put(Boolean.FALSE);
    }

}
