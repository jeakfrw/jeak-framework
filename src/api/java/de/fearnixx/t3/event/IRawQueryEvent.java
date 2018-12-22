package de.fearnixx.t3.event;

import de.fearnixx.t3.teamspeak.data.IDataHolder;
import de.fearnixx.t3.teamspeak.query.IQueryConnection;
import de.fearnixx.t3.teamspeak.query.IQueryRequest;

import java.util.List;

/**
 * Interface for less abstract query message access.
 * Try using {@link IQueryEvent} as much as possible.
 */
public interface IRawQueryEvent extends IEvent, IDataHolder {

    /**
     * The connection this event originated from.
     */
    IQueryConnection getConnection();

    /**
     * Main message interface.
     * Provides access to both previous and next message if present until the next "error" report.
     * Also provides access to the error report.
     *
     * Notifications will always use errorid=0.
     */
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

            int getHashCode();

            String getCaption();
        }
    }
}
