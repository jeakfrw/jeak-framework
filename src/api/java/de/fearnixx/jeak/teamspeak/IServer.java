package de.fearnixx.jeak.teamspeak;

import de.fearnixx.jeak.teamspeak.query.IQueryConnection;

/**
 * Created by MarkL4YG on 28-Jan-18
 */
public interface IServer {

    IQueryConnection getConnection();
}
