package de.fearnixx.t3.ts3.query;

import de.fearnixx.t3.event.query.IQueryEvent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 07-Jan-18
 */
public class PromisedRequest implements Future<IQueryEvent.IMessage> {

    private IQueryRequest request;
    private IQueryEvent.IMessage answer;

    public PromisedRequest(IQueryRequest request) {
        this.request = request;
    }

    public IQueryRequest getRequest() {
        return request;
    }

    public synchronized IQueryEvent.IMessage getAnswer() {
        return answer;
    }

    @Override
    public synchronized boolean isDone() {
        return answer != null;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Query requests cannot be cancelled!");
    }

    @Override
    public IQueryEvent.IMessage get() {
        throw new UnsupportedOperationException("Query requests may not be blocking! If you really need to do this use #get(long, TimeUnit)");
    }

    @Override
    public IQueryEvent.IMessage get(long timeout, TimeUnit unit) {
        if (timeout == 0 || unit == null)
            return getAnswer();

        try {
            long cTimeout = unit.toSeconds(timeout) * 4;
            while (!isDone() && cTimeout > 0) {
                Thread.sleep(250);
            }
            return getAnswer();
        } catch (InterruptedException e) {
        }

        return null;
    }

    protected synchronized void setAnswer(IQueryEvent.IMessage event) {
        this.answer = answer;
    }
}