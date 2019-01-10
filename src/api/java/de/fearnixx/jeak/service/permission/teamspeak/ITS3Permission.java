package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.IPermission;

/**
 * Representation of permission entries of TS3
 *
 * @author MarkL4YG
 */
public interface ITS3Permission extends IPermission {

    Boolean getSkip();

    Boolean getNegate();

    PriorityType getPriorityType();

    enum PriorityType {

        SERVER_GROUP(1),
        CLIENT(2),
        CHANNEL(3),
        CHANNEL_GROUP(4),
        CHANNEL_CLIENT(5);

        private Integer weight;

        PriorityType(Integer weight) {
            this.weight = weight;
        }

        public Integer getWeight() {
            return weight;
        }
    }
}
