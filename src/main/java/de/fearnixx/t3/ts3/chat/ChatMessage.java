package de.fearnixx.t3.ts3.chat;

import de.fearnixx.t3.ts3.keys.TargetType;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;
import de.fearnixx.t3.ts3.query.QueryMessageObject;

import java.util.Optional;

/**
 * Created by MarkL4YG on 11-Nov-17
 */
public class ChatMessage extends QueryMessageObject implements IChatMessage {

    @Override
    public Integer getInvokerID() {
        return Integer.parseInt(getProperty("invokerid").get());
    }

    @Override
    public String getInvokerUID() {
        return getProperty("invokeruid").get();
    }

    @Override
    public String getInvokerName() {
        return getProperty("invokername").get();
    }

    @Override
    public Optional<Integer> getTarget() {
        return Optional.ofNullable(getProperty("target").map(Integer::parseInt).orElse(null));
    }

    @Override
    public TargetType getTargetMode() {
        return TargetType.valueOf(Integer.parseInt(getProperty("targetmode").get()));
    }

    @Override
    public String getMessage() {
        return getProperty("msg").get();
    }
}
