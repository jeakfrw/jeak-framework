package de.fearnixx.t3.ts3.comm;

import de.fearnixx.t3.ts3.comm.except.CommException;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Life4YourGames on 06.07.17.
 */
public class CommChannel implements ICommChannel {

    private ILogReceiver log;

    private final Object lock = new Object();
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

    public String next(){
        synchronized (lock) {
            if (currentLock == null && handleQueue.size() > 0) {
                openWith(handleQueue.remove(0));
                messageQueue.clear();
                messageQueue.addAll(msgQueue.remove(0));
                return next();
            }
            if (handleQueue.size() > 0)
                return messageQueue.remove(0);
            return null;
        }
    }

    @Override
    public boolean openWith(ICommHandle h) {
        synchronized (lock) {
            if (currentLock != null && currentLock != h) return false;
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
        sendMessageWithThread(Thread.currentThread(), msg);
    }

    @Override
    public void sendMessage(ICommHandle h, String msg) {
        sendMessageWithHandle(h, msg);
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

    protected void sendMessageWithThread(Thread t, String s) {
        synchronized (lock) {
            if (currentLockThread != null && currentLockThread != t)
                return;
            else
                messageQueue.add(s);
        }
    }

    protected void sendMessageWithHandle(ICommHandle h, String s) {
        synchronized (lock) {
            if (currentLock != null && currentLock != h) {
                if (h == null) return;
                getListForHandle(h).add(s);
            } else
                messageQueue.add(s);
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
        try {
            while (true) {
                Thread.sleep(100);
                synchronized (lock) {
                    if (openWith(h))
                        break;
                    if (closeReason != null) throw new CommException.Closed(closeReason);
                }
            }
            sendMessage(h, msg);
        } catch (InterruptedException ex) {
            log.warning("Interrupted");
        }
    }

    public void invalidate(CommException.Closed.CloseReason reason) {
        synchronized (lock) {
            this.closeReason = reason;
        }
    }
}
