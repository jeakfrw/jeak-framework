package de.fearnixx.t3.event;

import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

import java.util.List;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public interface IQueryEvent extends IEvent {

    IQueryConnection getConnection();

    interface IAnswer extends IQueryEvent {

        IQueryRequest getRequest();

        List<IDataHolder> getChain();
    }

    interface INotification extends IQueryEvent, IDataHolder{
    }
}
