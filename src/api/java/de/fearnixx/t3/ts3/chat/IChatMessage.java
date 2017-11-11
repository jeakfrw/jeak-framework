package de.fearnixx.t3.ts3.chat;

import de.fearnixx.t3.ts3.keys.TargetType;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;

import java.util.Optional;

/**
 * Created by MarkL4YG on 11-Nov-17
 */
public interface IChatMessage extends IQueryMessageObject {

    TargetType getTargetMode();

    Integer getInvokerID();

    String getInvokerUID();

    String getInvokerName();

    Optional<Integer> getTarget();

    String getMessage();
}
