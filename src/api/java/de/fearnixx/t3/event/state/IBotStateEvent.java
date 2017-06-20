package de.fearnixx.t3.event.state;

import de.fearnixx.t3.event.IEvent;

/**
 * Created by MarkL4YG on 09.06.17.
 */
public interface IBotStateEvent extends IEvent.IBotEvent {

    public static interface IPluginsLoaded extends IBotStateEvent {

    }

    public static interface IPreConnect extends IBotStateEvent {

    }

    public static interface IPostConnect extends IBotStateEvent {

    }

    public static interface IPreShutdown extends IBotStateEvent {

    }

    public static interface IPostShutdown extends IBotStateEvent {

    }
}
