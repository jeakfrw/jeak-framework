package de.fearnixx.t3.service.comm;

import de.fearnixx.t3.query.IQueryConnection;
import de.fearnixx.t3.ts3.comm.except.CommException;
import de.fearnixx.t3.ts3.comm.ICommChannel;
import de.fearnixx.t3.ts3.comm.ICommHandle;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Life4YourGames on 06.07.17.
 */
public class CommChannel extends Thread implements ICommChannel {

    private ILogReceiver log;

    private final Object lock = new Object();;
    private boolean invalid;
    private final TargetType targetType;
    private final int id;

    private ICommHandle currentLock;
    private Thread currentLockThread;
    private final List<String> messageQueue = new ArrayList<>(10);
    private final List<ICommHandle> handleQueue = new ArrayList<>(10);
    private final List<List<String>> msgQueue = new ArrayList<>(10);
    private CommException.Closed.CloseReason closeReason = null;

    public CommChannel(ILogReceiver log, TargetType targetType, int id) {
        this.log = log;
        this.invalid = false;
        this.targetType = targetType;
        this.id = id;
    }

    @Override
    public boolean openWith(ICommHandle h) {
        synchronized (lock) {
            if (currentLock != null) return false;
            currentLock = h;
            currentLockThread = Thread.currentThread();
            return true;
        }
    }

    @Override
    public boolean closeWith(ICommHandle h) {
        synchronized (lock) {
            if (currentLock == null) return true;
            if (currentLock != h) return false;
            currentLock = null;
            currentLockThread = null;
            return true;
        }
    }

    @Override
    public TargetType getTargetType() {
        return targetType;
    }

    @Override
    public int getTargetID() {
        return id;
    }

    @Override
    public void sendMessage(String msg) {

    }

    @Override
    public void sendMessage(ICommHandle h, String msg) {

    }

    protected List<String> getListForHandle(ICommHandle h) {
        synchronized (lock) {
            int index = handleQueue.indexOf(h);
            List<String> l = null;
            if (index == -1) {
                l = new ArrayList<>();
                msgQueue.add(l);
                handleQueue.add(h);
            }
            return l;
        }
    }

    protected void sendMessageWithThread(ICommHandle h, Thread t, String s) {
        synchronized (lock) {
            if (currentLockThread != null && t != currentLockThread)
                if (h == null) return;
                else
                    getListForHandle(h).add(s);
        }
    }

    protected void pushMessage(CommMessage msg) {
        ICommHandle h;
        synchronized (lock) {
            if (invalid) return;
            h = currentLock;
        }
        if (h != null)
            h.getOnMessage().ifPresent(m -> m.accept(this, msg));
    }

    @Override
    public void sendMessageBlocking(ICommHandle h, String msg) throws CommException.Closed {
        synchronized (lock) {
            if (closeReason != null) throw new CommException.Closed(closeReason);
        }
    }

    public void invalidate(CommException.Closed.CloseReason reason) {
        synchronized (lock) {
            this.closeReason = reason;
        }
    }
}
