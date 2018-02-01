package de.fearnixx.t3.event;

import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public interface IRawQueryEvent extends IEvent, IDataHolder {

    IQueryConnection getConnection();
}
