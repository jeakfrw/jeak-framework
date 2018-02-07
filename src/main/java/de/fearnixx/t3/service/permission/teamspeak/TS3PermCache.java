package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.event.IRawQueryEvent.IMessage.IAnswer;

import java.time.LocalDateTime;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class TS3PermCache {

    private ITS3Permission.PriorityType type;
    private Integer subjectID;
    private Integer subjectID2;

    private IAnswer answer;
    private LocalDateTime timestamp;

    public TS3PermCache(Integer subjectID, Integer subjectID2, ITS3Permission.PriorityType type) {
        this.subjectID = subjectID;
        this.subjectID2 = subjectID2;
        this.type = type;
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
