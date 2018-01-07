package de.fearnixx.t3.service.perms.permission;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public enum PermSourceType {

    CLIENT(5),
    CHANNEL_CLIENT(4),
    CHANNEL_GROUP(3),
    CHANNEL(2),
    SERVER_GROUP(1),
    EXTERNAL(0);

    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    private PermSourceType(Integer priority) {
        this.priority = priority;
    }
}
