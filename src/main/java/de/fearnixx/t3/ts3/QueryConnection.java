package de.fearnixx.t3.ts3;

import de.mlessmann.logging.ILogReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Created by Life4YourGames on 22.05.17.
 */
public class QueryConnection extends Thread {

    private ILogReceiver log;

    private SocketAddress addr;
    private Socket mySock;
    private InputStream sIn;
    private OutputStream sOut;

    public QueryConnection(ILogReceiver log, String host, Integer port) {
        addr = new InetSocketAddress(host, port);
        mySock = new Socket();
    }

    public void open() throws IOException {
        mySock.connect(addr, 8000);
        log.severe("");
        sIn = mySock.getInputStream();
        sOut = mySock.getOutputStream();
        byte[] buffer = new byte[1024];
        int l = 0;
        int lfC = 0;
        while ((l = sIn.read(buffer)) != -1) {
            int lfPos = -1;
            for (int i = buffer.length - 1; i >= 0; i--) {
                if (buffer[i] == '\n') {
                    lfC = lfC + 1;
                    lfPos = i;
                }
            }
            if (lfC == 1) {
                String greet = new String(buffer, lfPos - 3, 3);
                if (!"TS3".equals(greet)) {
                    log.severe("ATTENTION TS3Connection for " + addr + " received an invalid greeting: " + greet);
                }
            } else if (lfC == 2) {
                log.info();
            }
        }
    }
}
