package de.fearnixx.t3.event.state;

import de.fearnixx.t3.event.IEvent;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public interface IBotStateEvent extends IEvent.IBotEvent {

    interface IPluginsLoaded extends IBotStateEvent {

    }

    interface IPreConnect extends IBotStateEvent {

    }

    interface IPostConnect extends IBotStateEvent {

    }

    interface IPreShutdown extends IBotStateEvent {

    }

    interface IPostShutdown extends IBotStateEvent {

    }
}
