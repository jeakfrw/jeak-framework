package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IRawQueryEvent.IMessage.IAnswer;

import java.time.LocalDateTime;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class TS3PermCache {

    private IAnswer answer;
    private LocalDateTime timestamp;

    public TS3PermCache() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setResponse(IAnswer answer) {
        this.answer = answer;
        this.timestamp = LocalDateTime.now();
    }

    public IAnswer getAnswer() {
        return answer;
    }
}
