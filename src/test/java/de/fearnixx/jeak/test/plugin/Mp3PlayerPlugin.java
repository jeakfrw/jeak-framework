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
import de.fearnixx.jeak.voice.connection.IVoiceConnection;
import de.fearnixx.jeak.voice.connection.IVoiceConnectionService;
import de.fearnixx.jeak.voice.event.IVoiceConnectionTextMessageEvent;
import de.fearnixx.jeak.voice.sound.AudioType;
import de.fearnixx.jeak.voice.sound.IAudioPlayer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

@JeakBotPlugin(id = "mpThreePlayer")
public class Mp3PlayerPlugin extends AbstractTestPlugin {

    @Inject
    private IVoiceConnectionService connectionService;

    @Inject
    private ICommandService commandService;

    @Inject
    private IDataCache dataCache;

    @Inject
    private IBot bot;

    private IVoiceConnection connection;

    public Mp3PlayerPlugin() {
        addTest("Mp3-Player");
    }

    private Map<String, Pair<IVoiceConnection, IAudioPlayer>> connectionsAndMp3Players = new HashMap<>();

    private int nextPlayerIndex = 1;
    private static final int MAX_PLAYER_COUNT = 5;

    private final List<String> sounds = new ArrayList<>();

    private File soundDir;

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        soundDir = new File(bot.getConfigDirectory(), "frw/voice/sounds");

        if (soundDir.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(soundDir.listFiles()))
                    .filter(f -> f.getName().toLowerCase().endsWith(".mp3"))
                    .map(File::getName)
                    .forEach(sounds::add);
        }

        commandService.registerCommand("mp3-player", ctx -> {
            if (nextPlayerIndex > MAX_PLAYER_COUNT) {
                throw new CommandException("Already reached the maximum amount of mp3-players!");
            }

            final String identifier = "Mp3-Player - " + nextPlayerIndex++;
            connection = connectionService.getVoiceConnection(identifier).orElseThrow();

            try {
                connection.connect();
            } catch (IOException | TimeoutException e) {
                //
            }

            final IAudioPlayer mp3AudioPlayer = connection.registerAudioPlayer(AudioType.MP3);

            connectionsAndMp3Players.put(identifier, new ImmutablePair<>(connection, mp3AudioPlayer));

            connection.sendToChannel(
                    dataCache.findClientByUniqueId(ctx.getRawEvent().getInvokerUID())
                            .orElseThrow(() -> new IllegalStateException("Client not found in cache"))
                            .getChannelID()
            );
        });

        success("test");
    }

    @Listener
    public void textMessageToMp3Player(IVoiceConnectionTextMessageEvent event) {
        String message = event.getMessage();

        if (!message.startsWith("!")) {
            return;
        }

        String[] msgSplit = message.substring(1).split(" ");

        String cmd = msgSplit[0];

        String param = "";
        if (msgSplit.length > 1) {
            param = msgSplit[1];
        }

        IAudioPlayer mp3AudioPlayer = connectionsAndMp3Players.get(event.getVoiceConnectionIdentifier()).getValue();

        switch (cmd) {

            case "play":
                String fileName;
                if (param.isEmpty()) {
                    fileName = sounds.get(new Random().nextInt(sounds.size()));
                } else {
                    String soundToPlay = param;

                    if (!soundToPlay.toLowerCase().endsWith(".mp3")) {
                        soundToPlay += ".mp3";
                    }

                    if (sounds.contains(soundToPlay)) {
                        fileName = soundToPlay;
                    } else {
                        return;
                    }
                }

                try {
                    mp3AudioPlayer.setAudioFile(soundDir, fileName);

                    if (!mp3AudioPlayer.isPlaying()) {
                        mp3AudioPlayer.play();
                    }

                } catch (FileNotFoundException e) {
                    //This is not possible
                }

                break;

            case "pause":
                mp3AudioPlayer.pause();
                break;
            case "resume":
                mp3AudioPlayer.resume();
                break;
        }
    }
}
