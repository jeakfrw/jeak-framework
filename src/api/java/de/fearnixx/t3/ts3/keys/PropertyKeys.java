package de.fearnixx.t3.ts3.keys;

/**
 * Created by MarkL4YG on 15.06.17.
 * A collection of Strings in order to minimize the amount of hardcoded Strings needed in plugins/implementation
 * Simplifies optimization
 *
 * At some point these may be replaced with enums for faster processing
 * If possible that'll only require plugins to recompile once without changes
 * or change from PropertyKeys to PropertyEnums
 */
public class PropertyKeys {

    /**
     * All available client properties (clientlist response)
     */
    public static class Client {

    }

    /**
     * All available client properties (clientdblist response)
     */
    public static class ClientDB {

    }

    /**
     * All available channel properties
     */
    public static class Channel {
        public static final String NAME = "channel_name";
        public static final String ID = "cid";
        public static final String PARENT = "pid";
        public static final String ORDER = "channel_order";

        public static final String TALK_POWER = "channel_needed_talk_power";
        public static final String TOPIC = "channel_topic";

        public static final String CLIENT_COUNT = "total_clients";
        public static final String MAX_CLIENTS = "channel_maxclients";
        public static final String CLIENT_COUNT_FAMILY = "total_clients_family";
        public static final String MAX_CLIENTS_FAMILY = "channel_maxfamilyclients";

        public static final String CODEC = "channel_codec";
        public static final String QUALITY = "channel_codec_quality";

        public static final String FLAG_DEFAULT = "channel_flag_default";
        public static final String FLAG_PASSWORD = "channel_flag_password";
        public static final String FLAG_PERMANENT = "channel_flag_permanent";
        public static final String FLAG_SEMI_PERMANENT = "channel_flag_semi_permanent";
    }
}
