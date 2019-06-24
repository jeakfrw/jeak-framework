package de.fearnixx.jeak.teamspeak;

/**
 * A collection of Strings in order to minimize the amount of hardcoded Strings needed in plugins/implementation
 * Simplifies optimization
 * Also simplifies adaptation to small query changes
 *
 * These values are taken directly from the server query manual backpages.
 * However, since the manual is dated here and there, we have to make adjustments to those values.
 */
public abstract class PropertyKeys {

    /**
     * All available keys used in TextMessage stuff
     */
    public abstract static class TextMessage {

        private TextMessage() {
        }

        public static final String TARGET_TYPE = "targetmode";
        public static final String TARGET_ID = "target";
        public static final String SOURCE_ID = "invokerid";
        public static final String SOURCE_NICKNAME = "invokername";
        public static final String SOURCE_UID = "invokeruid";
        public static final String MESSAGE = "msg";
    }

    /**
     * All available client properties.
     * As defined by the server query manual.
     */
    public abstract static class Client {

        private Client() {
        }

        public static final String ID = "clid";
        public static final String CHANNEL_ID = Channel.ID;
        public static final String DBID = "client_database_id";
        public static final String UID = "client_unique_identifier";
        public static final String NICKNAME = "client_nickname";
        public static final String NICKNAME_PHONETIC = "client_nickname_phonetic";
        public static final String DESCRIPTION = "client_description";
        public static final String TYPE = "client_type";
        public static final String PLATFORM = "client_platform";
        public static final String VERSION = "client_version";
        public static final String ICON_ID = "client_icon_id";
        public static final String COUNTRY = "client_country";

        public static final String CHANNEL_GROUP = "client_channel_group_id";
        public static final String CHANNEL_GROUP_SOURCE = "client_channel_group_inherited_channel";
        public static final String GROUPS = "client_servergroups";

        public static final String IDLE_TIME = "client_idle_time";
        public static final String CREATED_TIME = "client_created";
        public static final String LAST_JOIN_TIME = "client_lastconnected";
        public static final String UNREAD_MESSAGES = "client_unread_messages";
        public static final String IPV4_ADDRESS = "connection_client_ip";

        public static final String FLAG_AWAY = "client_away";
        public static final String AWAY_MESSAGE = "client_away_message";
        public static final String FLAG_TALKING = "client_flag_talking";
        public static final String TALKPOWER = "client_talk_power";
        public static final String FLAG_TALKER = "client_is_talker";
        public static final String FLAG_PRIO_TALKER = "client_is_priority_speaker";
        public static final String FLAG_COMMANDER = "client_is_channel_commander";

        public static final String FLAG_RECORDING = "client_is_recording";
        public static final String IOIN = "client_input_hardware";
        public static final String IOIN_MUTED = "client_input_muted";
        public static final String IOOUT = "client_output_hardware";
        public static final String IOOUT_MUTED = "client_output_muted";
    }

    /**
     * All available client properties (clientdblist response)
     */
    public abstract static class DBClient {

        private DBClient() {
        }

        public static final String UID = Client.UID;
        public static final String UID64 = "client_base64HashClientUID";
        public static final String NICKNAME = Client.NICKNAME;
        public static final String DBID = Client.DBID;
        public static final String CREATED_TIME = Client.CREATED_TIME;
        public static final String LAST_JOIN_TIME = Client.LAST_JOIN_TIME;
        public static final String TOTAL_CONNECTIONS = "client_totalconnections";
        public static final String ICON_ID = Client.ICON_ID;
        public static final String AVATAR = "client_flag_avatar";
        public static final String DESCRIPTION = "client_description";
        public static final String UPLOAD_TOTAL = "client_total_bytes_uploaded";
        public static final String UPLOAD_MONTH = "client_month_bytes_uploaded";
        public static final String DOWNLOAD_TOTAL = "client_total_bytes_downloaded";
        public static final String DOWNLOAD_MONTH = "client_month_bytes_downloaded";
        public static final String LAST_IP = "client_lastip";
    }

    /**
     * All available channel properties
     */
    public abstract static class Channel {

        private Channel() {
        }

        public static final String NAME = "channel_name";
        public static final String ID = "cid";
        public static final String PARENT = "pid";
        public static final String ORDER = "channel_order";

        public static final String TALK_POWER = "channel_needed_talk_power";
        public static final String TOPIC = "channel_topic";
        public static final String DESCRIPTION = "channel_description";
        public static final String ICON_ID = "channel_icon_id";
        public static final String PASSWORD = "channel_password";

        public static final String CLIENT_COUNT = "total_clients";
        public static final String MAX_CLIENTS = "channel_maxclients";
        public static final String FLAG_MAX_CLIENTS_UNLIMITED = "channel_flag_maxclients_unlimited";
        public static final String CLIENT_COUNT_FAMILY = "total_clients_family";
        public static final String MAX_CLIENTS_FAMILY = "channel_maxfamilyclients";
        public static final String FLAG_MAX_CLIENTS_FAMALY_UNLIMITED = "channel_flag_maxfamilyclients_unlimited";
        public static final String FLAG_MAX_CLIENTS_FAMALY_INHERITED = "channel_flag_maxfamilyclients_inherited";

        public static final String CODEC = "channel_codec";
        public static final String QUALITY = "channel_codec_quality";

        public static final String FLAG_DEFAULT = "channel_flag_default";
        public static final String FLAG_PASSWORD = "channel_flag_password";
        public static final String FLAG_PERMANENT = "channel_flag_permanent";
        public static final String FLAG_SEMI_PERMANENT = "channel_flag_semi_permanent";
        public static final String FLAG_TEMPORARY = "channel_flag_temporary";
        public static final String FLAG_UNENCRYPTED = "channel_codec_is_unencrypted";

        public static final String DELETE_DELAY = "channel_delete_delay";
    }

    public abstract static class Permission {

        private Permission() {
        }

        public static final String ID = "permid";
        public static final String ID_SHORT = "p";
        public static final String VALUE = "permval";
        public static final String VALUE_SHORT = "v";
        public static final String FLAG_NEGATED = "permnegated";
        public static final String FLAG_NEGATED_SHORT = "n";
        public static final String FLAG_SKIP = "permskip";
        public static final String FLAG_SKIP_SHORT = "s";
    }
}
