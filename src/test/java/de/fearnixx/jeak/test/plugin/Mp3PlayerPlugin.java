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
import java.net.URL;
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

        commandService.registerCommand("mp3-file-player", ctx -> {
            if (nextPlayerIndex > MAX_PLAYER_COUNT) {
                throw new CommandException("Already reached the maximum amount of audio-players!");
            }

            createVoiceConnection(
                    "Mp3-Player - " + nextPlayerIndex++, ctx.getRawEvent().getInvokerUID(), AudioType.MP3
            );
        });

        commandService.registerCommand("web-radio-player", ctx -> {
            if (nextPlayerIndex > MAX_PLAYER_COUNT) {
                throw new CommandException("Already reached the maximum amount of audio-players!");
            }

            createVoiceConnection(
                    "Web-Radio-Player - " + nextPlayerIndex++, ctx.getRawEvent().getInvokerUID(), AudioType.WEBRADIO
            );
        });

        success("test");
    }

    private void createVoiceConnection(String identifier, String uuid, AudioType audioType) {
        connectionService.requestVoiceConnection(identifier, optConnection -> {

                    IVoiceConnection connection = optConnection.orElseThrow();

                    try {
                        connection.connect();
                    } catch (IOException | TimeoutException e) {
                        //
                    }

                    final IAudioPlayer audioPlayer = connection.registerAudioPlayer(audioType);

                    connectionsAndMp3Players.put(identifier, new ImmutablePair<>(connection, audioPlayer));

                    dataCache.findClientByUniqueId(uuid).ifPresent(
                            client -> connection.sendToChannel(client.getChannelID())
                    );
                }
        );
    }

    @Listener
    public void textMessageToAudioPlayer(IVoiceConnectionTextMessageEvent event) {
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

                if (mp3AudioPlayer.getAudioType() == AudioType.MP3) {
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
                    } catch (FileNotFoundException e) {
                        //This is not possible
                    }
                } else if (mp3AudioPlayer.getAudioType() == AudioType.WEBRADIO) {
                    try {
                        if (param.startsWith("[URL]")) {
                            param = param.substring(5, param.length() - 6);
                        }

                        mp3AudioPlayer.setAudioFile(new URL(param).openStream());
                    } catch (IOException e) {
                        throw new IllegalArgumentException("The given string is not a valid URL!");
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "The audio type " + mp3AudioPlayer.getAudioType()
                                    + " is not supported by this plugin!"
                    );
                }

                mp3AudioPlayer.play();
                break;
            case "volume":
                mp3AudioPlayer.setVolume(Double.valueOf(param));
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
