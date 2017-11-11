package de.fearnixx.t3.ts3.chat;

import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.keys.TargetType;
import de.fearnixx.t3.ts3.query.IQueryConnection;
import de.fearnixx.t3.ts3.query.IQueryMessage;
import de.fearnixx.t3.ts3.query.IQueryRequest;

/**
 * Created by MarkL4YG on 11-Nov-17
 */
public class ChatConversation implements IChatConversation {

    private IQueryConnection connection;
    private Integer targetID;
    private TargetType targetType;

    private final Object lock = new Object();

    public ChatConversation(IQueryConnection connection, Integer targetID, TargetType targetType) {
        this.connection = connection;
        this.targetID = targetID;
        this.targetType = targetType;
    }

    @Override
    public Integer getTargetID() {
        return targetID;
    }

    @Override
    public TargetType getTargetType() {
        return targetType;
    }

    @Override
    public boolean openWith(IChatHandle handle) {
        return false;
    }

    @Override
    public boolean closeWith(IChatHandle handle) {
        return false;
    }

    @Override
    @Deprecated
    public void sendMessage(String message) {
        IQueryRequest req = IQueryRequest.builder()
                .command("sendtextmessage")
                .addKey(PropertyKeys.TextMessage.MESSAGE, message)
                .addKey(PropertyKeys.TextMessage.TARGET_TYPE, targetType.getQueryNum())
                .addKey(PropertyKeys.TextMessage.TARGET_ID, targetID)
                .build();
        connection.sendRequest(req);
    }

    @Override
    public void sendMessage(IChatHandle handle, String message) {
        sendMessage(message);
    }

    public void shutdown() {

    }
}
