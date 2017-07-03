package de.fearnixx.t3.service.db;

import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.query.IQueryMessage;
import de.fearnixx.t3.query.IQueryMessageObject;
import de.fearnixx.t3.query.IQueryRequest;
import de.fearnixx.t3.ts3.ITS3Server;
import de.fearnixx.t3.ts3.client.IDBClient;
import de.fearnixx.t3.ts3.client.TS3DBClient;
import de.mlessmann.logging.ILogReceiver;

import java.util.Optional;

/**
 * Created by MarkL4YG on 30.06.17.
 */
public class DBReader implements IDBReader {

    private ILogReceiver log;
    private ITS3Server server;

    private boolean terminated = false;
    private final Object lock = new Object();

    public DBReader(ILogReceiver log, ITS3Server server) {
        this.log = log;
        this.server = server;
    }

    @Override
    public Optional<IDBClient> getClientDBInfo(int cldbid) {
        synchronized (lock) {
            if (terminated) return Optional.empty();
        }

        IQueryRequest req = IQueryRequest.builder()
                .command("clientdbinfo")
                .addKey("cldbid", Integer.valueOf(cldbid).toString())
                .build();
        final IQueryEvent.IMessage[] qE = new IQueryEvent.IMessage[]{null};
        server.getConnection().sendRequest(req, e -> {
            synchronized (lock) {
                qE[0] = e;
            }
        });
        try {
            while (!terminated) {
                Thread.sleep(250);
                synchronized (lock) {
                    if (qE[0] != null) break;
                }
            }
        } catch (InterruptedException e) {
            log.warning("Interrupted");
            return Optional.empty();
        }
        IQueryMessage msg = qE[0].getMessage();
        IQueryMessageObject.IError e = msg.getError();
        if (e.getID() != 0) {
            log.info("ClientDBReq for cldbid", cldbid, "returned:", e.getID(),e.getMessage());
            return Optional.empty();
        }
        if (msg.getObjectCount() != 1) {
            log.info("Wrong number of QMSG-objects in ClientDBReq for cldbid", cldbid, ':', msg.getObjectCount());
            return Optional.empty();
        }
        log.finer("Successful ClientDBReq for cldbid", cldbid);
        IQueryMessageObject o = msg.getObject(0);
        TS3DBClient c = new TS3DBClient();
        c.copyFrom(o);
        return Optional.of(c);
    }

    public void shutdown() {
        synchronized (lock) {
            terminated = true;
        }
    }
}
