import de.fearnixx.t3.events.state.IBotStateEvent;
import de.fearnixx.t3.main.IT3Bot;
import de.fearnixx.t3.reflect.annotation.Inject;
import de.fearnixx.t3.reflect.annotation.Listener;
import de.fearnixx.t3.reflect.annotation.T3BotPlugin;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

import java.util.Map;

/**
 * Created by MarkL4YG on 15.06.17.
 */
@T3BotPlugin(id = "channelmgmt", version = "1.0.0")
public class ChannelMgmt {

    @Inject
    public ILogReceiver log;

    @Inject
    public IT3Bot bot;

    @Inject(id = "subChannels")
    public ConfigLoader subChannelLoader;

    private Map<String, Integer> subChannelCache;

    public ChannelMgmt() {

    }

    @Listener
    public void onLoaded(IBotStateEvent.IPluginsLoaded event) {

    }
}
