package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.IBot;
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
import de.fearnixx.jeak.voice.sound.IMp3AudioPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@JeakBotPlugin(id = "voiceTest")
public class VoiceConnectionTestPlugin extends AbstractTestPlugin {

    @Inject
    private IClientConnectionService connectionService;

    @Inject
    private IDataCache dataCache;

    @Inject
    private ITaskService taskService;

    @Inject
    private IBot bot;

    private IClientConnection connection;

    public VoiceConnectionTestPlugin() {
        addTest("test");
    }

    @Listener
    public void onConnect(IBotStateEvent.IConnectStateEvent.IPostConnect event) throws Exception {
        connection = connectionService.getClientConnection("test");

        connection.connect();

        final IMp3AudioPlayer mp3AudioPlayer = connection.registerMp3AudioPlayer();

        mp3AudioPlayer.setAudioFile(new FileInputStream(new File(bot.getConfigDirectory(), "frw/voice/sounds/EpicSaxGuy.mp3")));

        new Thread(() -> {
            mp3AudioPlayer.start();
            mp3AudioPlayer.play();
        }).start();

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
