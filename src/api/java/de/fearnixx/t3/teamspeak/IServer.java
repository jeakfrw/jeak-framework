package de.fearnixx.t3.teamspeak;

import de.fearnixx.t3.teamspeak.query.IQueryConnection;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IServer {

    IQueryConnection getConnection();
}
