package de.fearnixx.t3.service.comm;

import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.event.query.QueryNotificationEvent;
import de.fearnixx.t3.event.server.ITS3ServerEvent;
import de.fearnixx.t3.query.IQueryConnection;
import de.fearnixx.t3.query.IQueryRequest;
import de.fearnixx.t3.reflect.annotation.Listener;
import de.fearnixx.t3.ts3.ITS3Server;
import de.fearnixx.t3.ts3.channel.IChannel;
import de.fearnixx.t3.ts3.comm.ICommChannel;
import de.fearnixx.t3.ts3.comm.ICommManager;
import de.fearnixx.t3.ts3.comm.except.CommException;
import de.fearnixx.t3.ts3.keys.NotificationType;
import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.mlessmann.logging.ILogReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Life4YourGames on 06.07.17.
 */
public class CommManager extends Thread implements ICommManager {

    private ILogReceiver log;
    private IQueryConnection conn;

    private final Object lock = new Object();
    private boolean terminated = false;

    private final CommChannel serverChannel;
    private List<IChannel> channelList;
    private final Map<Integer, CommChannel> channelChannel;
    private final Map<Integer, CommChannel> clientChannel;

    public CommManager(ILogReceiver log, IQueryConnection conn) {
        this.log = log;
        this.conn = conn;
        serverChannel = new CommChannel(log.getChild("sc"),TargetType.SERVER, 0);
        channelChannel = new HashMap<>();
        clientChannel = new HashMap<>();
    }

    protected CommChannel getChannelChannel(Integer id) {
        return channelChannel.getOrDefault(id, null);
    }

    protected CommChannel getClientChannel(Integer id) {
        return clientChannel.getOrDefault(id, null);
    }

    @Override
    public ICommChannel getCommChannel(NotificationType type, int id) {
        return null;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(100);
                synchronized (lock) {
                    if (terminated) break;
                    this.checkSend(0, serverChannel);
                    channelChannel.forEach(this::checkSend);
                    clientChannel.forEach(this::checkSend);
                }
            }
        } catch (InterruptedException e) {
            log.warning("Interrupted");
            terminate();
        }
    }

    protected void checkSend(Integer id, CommChannel c) {
        String msg = c.next();
        if (id == 0) id = conn.getInstanceID();
        if (msg == null) return;
        IQueryRequest r = IQueryRequest.builder()
                .command("sendtextmessage")
                .addKey("targetmode", c.getTargetType().getQueryNum().toString())
                .addKey("target", id.toString())
                .addKey("msg", msg)
                .build();
        conn.sendRequest(r, e -> {
            if (e.getMessage().getError().getID() != 0) {
                log.warning("Failed to send message to: ", id, ' ', c.getTargetType(), ':', e.getMessage().getError());
            }
        });
    }

    public void terminate() {
        synchronized (lock) {
            terminated = true;
            conn = null;
        }
    }

    @Listener
    public void onMessage(IQueryEvent.INotification.ITextMessage event) {
        CommMessage m = ((CommMessage) event.getTextMessage());
        TargetType t = m.getSourceType();
        int id = m.getSourceID();
        CommChannel c = null;
        synchronized (lock) {
            switch (t) {
                case SERVER:
                    c = serverChannel;
                    break;
                case CHANNEL:
                    c = getChannelChannel(id);
                    break;
                case CLIENT:
                    c = getClientChannel(id);
                    break;
            }
        }
        if (c != null)
            c.pushMessage(m);
    }

    @Listener
    public void onClientEnter(IQueryEvent.INotification.ITargetClient.IClientEnterView event) {
        Integer clid = event.getTarget().getClientID();
        TargetType t = TargetType.CLIENT;
        synchronized (lock) {
            CommChannel c = new CommChannel(log.getChild("cl" + clid), t, clid);
            clientChannel.put(clid, c);
        }
    }

    @Listener
    public void onClientLeave(IQueryEvent.INotification.ITargetClient.IClientLeftView event) {
        CommChannel c = getClientChannel(event.getTarget().getClientID());
        synchronized (lock) {
            clientChannel.remove(c.getTargetID()).invalidate(CommException.Closed.CloseReason.CLIENT_DISCONNECTED);
        }
    }

    @Listener
    public void onChannelsUpdated(ITS3ServerEvent.IDataEvent.IChannelsUpdated event) {
        synchronized (lock) {
            channelList.stream()
                    .filter(c -> c.getPersistence() == IChannel.ChannelPersistence.DELETED)
                    .forEach(c -> channelChannel.remove(c.getID()).invalidate(CommException.Closed.CloseReason.CHANNEL_DELETED));
            channelList = event.getServer().getChannelList();
            channelList.stream()
                    .filter(c -> getChannelChannel(c.getID()) == null)
                    .forEach(c -> {
                        Integer id = c.getID();
                        channelChannel.put(id, new CommChannel(log.getChild("cc" + id), TargetType.CHANNEL, id));
                    });
        }
    }
}
