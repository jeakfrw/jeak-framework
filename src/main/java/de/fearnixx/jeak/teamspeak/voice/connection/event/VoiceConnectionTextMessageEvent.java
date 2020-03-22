package de.fearnixx.jeak.teamspeak.voice.connection.event;

import com.github.manevolent.ts3j.event.TextMessageEvent;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.voice.event.IVoiceConnectionTextMessageEvent;

public class VoiceConnectionTextMessageEvent implements IVoiceConnectionTextMessageEvent {

    private final String identifier;
    private TextMessageEvent ts3jTextMessageEvent;

    public VoiceConnectionTextMessageEvent(String identifier, TextMessageEvent ts3jTextMessageEvent) {
        this.identifier = identifier;
        this.ts3jTextMessageEvent = ts3jTextMessageEvent;
    }

    @Override
    public String getInvokerUid() {
        return ts3jTextMessageEvent.getInvokerUniqueId();
    }

    @Override
    public String getMessage() {
        return ts3jTextMessageEvent.getMessage();
    }

    @Override
    public String getVoiceConnectionIdentifier() {
        return identifier;
    }

    @Override
    public Integer getInvokerId() {
        return ts3jTextMessageEvent.getInvokerId();
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.fromQueryNum(ts3jTextMessageEvent.getTargetMode().getIndex());
    }
}
