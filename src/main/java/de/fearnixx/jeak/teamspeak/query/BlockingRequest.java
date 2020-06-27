package de.fearnixx.jeak.teamspeak.query;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BlockingRequest {

    private static final Logger logger = LoggerFactory.getLogger(BlockingRequest.class);
    private static final Integer REQ_MAX_SECONDS = Main.getProperty("jeak.query.blockingTimeoutSecs", 120);

    private final IQueryRequest originalRequest;
    private final Object monitor = new Object();
    private long maxThreshold = TimeUnit.SECONDS.toMillis(REQ_MAX_SECONDS);
    private IQueryEvent.IAnswer answer;

    public BlockingRequest(IQueryRequest originalRequest) {
        this.originalRequest = originalRequest;
        this.originalRequest.onDone(this::onAnswer);
    }

    public BlockingRequest(IQueryRequest originalRequest, int time, TimeUnit timeUnit) {
        this(originalRequest);
        maxThreshold = timeUnit.toMillis(time);
    }

    public boolean waitForCompletion() {
        synchronized (monitor) {
            try {
                logger.trace("Entering WAIT for request completion.");
                monitor.wait(maxThreshold);
                return answer != null;
            } catch (IllegalMonitorStateException e) {
                throw new RuntimeException("Failed to await request completion!", e);
            } catch (InterruptedException e) {
                logger.info("Blocking request interrupted during wait.");
                return false;
            }
        }
    }

    private void onAnswer(IQueryEvent.IAnswer answer) {
        synchronized (monitor) {
            this.answer = answer;
            monitor.notify();
        }
    }

    public IQueryRequest getOriginalRequest() {
        return originalRequest;
    }

    public IQueryEvent.IAnswer getAnswer() {
        synchronized (monitor) {
            return answer;
        }
    }
}
