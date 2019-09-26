package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.fearnixx.jeak.voice.connection.IClientConnection;
import de.fearnixx.jeak.voice.connection.IClientConnectionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@JeakBotPlugin(id = "voiceTest")
public class VoiceConnectionTestPlugin extends AbstractTestPlugin {

    @Inject
    private IClientConnectionService connectionService;

    @Inject
    private IDataCache dataCache;

    @Inject
    private ITaskService taskService;
    private IClientConnection connection;

    public VoiceConnectionTestPlugin() {
        addTest("test");
    }

    @Listener
    public void onConnect(IBotStateEvent.IConnectStateEvent.IPostConnect event) throws IOException, TimeoutException {
        connection = connectionService.getClientConnection("test");

        connection.connect();

        taskService.scheduleTask(ITask.builder().name("voice-test-moving").interval(5, TimeUnit.SECONDS).runnable(() -> {
            List<IChannel> channels = new ArrayList<>(dataCache.getChannels());

            if (!channels.isEmpty()) {
                Collections.shuffle(channels);
                connection.sendToChannel(channels.get(0).getID());
            }
        }).build());

        success("test");
    }
}
