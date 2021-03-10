package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;

import java.time.LocalDateTime;

/**
 * @author MarkL4YG
 * @since 1.0
 */
public class TS3PermCache {

    private IQueryEvent.IAnswer answer;
    private LocalDateTime timestamp;

    public TS3PermCache() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setResponse(IQueryEvent.IAnswer answer) {
        this.answer = answer;
        this.timestamp = LocalDateTime.now();
    }

    public IQueryEvent.IAnswer getAnswer() {
        return answer;
    }
}
