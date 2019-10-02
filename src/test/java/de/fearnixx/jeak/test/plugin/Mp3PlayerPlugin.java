package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.IBot;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.CommandException;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import de.fearnixx.jeak.voice.connection.IClientConnection;
import de.fearnixx.jeak.voice.connection.IClientConnectionService;
import de.fearnixx.jeak.voice.sound.IMp3AudioPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@JeakBotPlugin(id = "mpThreePlayer")
public class Mp3PlayerPlugin extends AbstractTestPlugin {

    @Inject
    private IClientConnectionService connectionService;

    @Inject
    private ICommandService commandService;

    @Inject
    private IDataCache dataCache;

    @Inject
    private IBot bot;

    private IClientConnection connection;

    public Mp3PlayerPlugin() {
        addTest("Mp3-Player");
    }

    private final List<String> sounds = new ArrayList<>();

    @Listener
    public void onConnect(IBotStateEvent.IConnectStateEvent.IPostConnect event) throws Exception {
        File soundDir = new File(bot.getConfigDirectory(), "frw/voice/sounds");

        if (soundDir.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(soundDir.listFiles()))
                    .filter(f -> f.getName().toLowerCase().endsWith(".mp3"))
                    .map(File::getName)
                    .forEach(sounds::add);
        }

        if (!sounds.isEmpty()) {
            connection = connectionService.getClientConnection("Mp3-Player");
            connection.connect();

            final IMp3AudioPlayer mp3AudioPlayer = connection.registerMp3AudioPlayer();

            new Thread(mp3AudioPlayer::start).start();

            commandService.registerCommand("play", ctx -> {
                String fileName;
                if (ctx.getArguments().isEmpty()) {
                    fileName = sounds.get(new Random().nextInt(sounds.size()));
                } else {
                    String soundToPlay = ctx.getArguments().get(0);

                    if (!soundToPlay.toLowerCase().endsWith(".mp3")) {
                        soundToPlay += ".mp3";
                    }

                    if (sounds.contains(soundToPlay)) {
                        fileName = soundToPlay;
                    } else {
                        throw new CommandException("This sound does not exist!");
                    }
                }

                try {
                    connection.sendToChannel(
                            dataCache.findClientByUniqueId(ctx.getRawEvent().getInvokerUID())
                                    .orElseThrow(() -> new IllegalStateException("Client not found in cache"))
                                    .getChannelID()
                    );

                    mp3AudioPlayer.setAudioFile(bot.getConfigDirectory(), fileName);

                    if (!mp3AudioPlayer.isPlaying()) {
                        mp3AudioPlayer.play();
                    }

                } catch (FileNotFoundException e) {
                    //This is not possible
                }
            });

            commandService.registerCommand("pause", ctx -> mp3AudioPlayer.pause());
            commandService.registerCommand("resume", ctx -> mp3AudioPlayer.resume());

            success("test");
        }
    }
}
