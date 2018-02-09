package de.fearnixx.t3.event;

import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

import java.util.List;

/**
 * Created by MarkL4YG on 01-Feb-18
 */
public interface IRawQueryEvent extends IEvent, IDataHolder {

    IQueryConnection getConnection();

    interface IMessage extends IRawQueryEvent {

        boolean hasNext();

        boolean hasPrevious();

        IMessage getNext();

        IMessage getPrevious();

        List<IMessage> toList();

        IErrorMessage getError();

        interface IAnswer extends IMessage {

            IQueryRequest getRequest();
        }

        interface IErrorMessage extends IAnswer {

            Integer getCode();

            String getMessage();
        }

        interface INotification extends IMessage {

            String getCaption();
        }
    }
}
