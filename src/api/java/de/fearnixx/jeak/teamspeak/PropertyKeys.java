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
        public static final String VALUE_UNABBREVIATED = "permvalue";
        public static final String FLAG_NEGATED = "permnegated";
        public static final String FLAG_NEGATED_SHORT = "n";
        public static final String FLAG_SKIP = "permskip";
        public static final String FLAG_SKIP_SHORT = "s";
    }

    /**
     * Properties available in the <em>serverinfo</em> response.
     */
    public abstract static class ServerInfo {

        private ServerInfo() {
        }

        public static final String ID = "virtualserver_id";
        public static final String MACHINE_ID = "virtualserver_machine_id";
        public static final String UNIQUE_IDENTIFIER = "virtualserver_unique_identifier";
        public static final String AUTOSTART = "virtualserver_autostart";
        public static final String NEEDED_IDENTITY_SECURITY_LEVEL = "virtualserver_needed_identity_security_level";
        public static final String ASK_PRIVILEGE_KEY = "virtualserver_ask_for_privilegekey";

        public static final String IP = "virtualserver_ip";
        public static final String PORT = "virtualserver_port";
        public static final String NICKNAME = "virtualserver_nickname";
        public static final String STATUS = "virtualserver_status";

        public static final String NAME = "virtualserver_name";
        public static final String NAME_PHONETIC = "virtualserver_name_phonetic";
        public static final String WELCOME_MESSAGE = "virtualserver_welcomemessage";
        public static final String PLATFORM = "virtualserver_platform";
        public static final String VERSION = "virtualserver_version";
        public static final String MIN_CLIENT_VERSION = "virtualserver_min_client_version";
        public static final String MIN_ANDROID_VERSION = "virtualserver_min_android_version";
        public static final String MIN_IOS_VERSION = "virtualserver_min_ios_version";
        public static final String MAX_CLIENTS = "virtualserver_maxclients";
        public static final String RESERVED_SLOTS = "virtualserver_reserved_slots";
        public static final String PASSWORD = "virtualserver_password";
        public static final String CLIENTS_ONLINE = "virtualserver_clientsonline";
        public static final String CHANNELS_ONLINE = "virtualserver_channelsonline";
        public static final String CREATED_TSP = "virtualserver_created";
        public static final String UPTIME_SECS = "virtualserver_uptime";
        public static final String CODEC_ENCRYPTION_MODE = "virtualserver_codec_encryption_mode";
        public static final String TEMP_CHANNEL_DEFAULT_DELETE_DELAY = "virtualserver_channel_temp_delete_delay_default";

        public static final String SHOW_ON_WEBLIST = "virtualserver_weblist_enabled";
        public static final String ICON_ID = "virtualserver_icon_id";
        public static final String HOST_MESSAGE = "virtualserver_hostmessage";
        public static final String HOST_MESSAGE_MODE = "virtualserver_hostmessage_mode";

        public static final String HOST_BANNER_URL = "virtualserver_hostbanner_url";
        public static final String HOST_BANNER_GFX_URL = "virtualserver_hostbanner_gfx_url";
        public static final String HOST_BANNER_GFX_INTERVAL = "virtualserver_hostbanner_gfx_interval";
        public static final String HOST_BANNER_MODE = "virtualserver_hostbanner_mode";

        public static final String HOST_BUTTON_TOOLTIP = "virtualserver_hostbutton_tooltip";
        public static final String HOST_BUTTON_URL = "virtualserver_hostbutton_url";
        public static final String HOST_BUTTON_GFX_URL = "virtualserver_hostbutton_gfx_url";

        public static final String FILETRANSFER_BASE_DIRECTORY = "virtualserver_filebase";
        public static final String DEFAULT_SERVER_GROUP = "virtualserver_default_server_group";
        public static final String DEFAULT_CHANNEL_GROUP = "virtualserver_default_channel_group";
        public static final String DEFAULT_CHANNEL_ADMIN_GROUP = "virtualserver_default_channel_admin_group";

        public static final String MAX_DOWNLOAD_TOTAL = "virtualserver_max_download_total_bandwidth";
        public static final String MAX_UPLOAD_TOTAL = "virtualserver_max_upload_total_bandwidth";
        public static final String DOWNLOAD_QUOTA = "virtualserver_download_quota";
        public static final String UPLOAD_QUOTA = "virtualserver_upload_quota";
        public static final String MONTH_BYTES_DOWNLOAD = "virtualserver_month_bytes_downloaded";
        public static final String MONTH_BYTES_UPLOAD = "virtualserver_month_bytes_uploaded";
        public static final String TOTAL_BYTES_DOWNLOAD = "virtualserver_total_bytes_downloaded";
        public static final String TOTAL_BYTES_UPLOAD = "virtualserver_total_bytes_uploaded";

        public static final String COMPLAIN_AUTOBAN_THRESHOLD = "virtualserver_complain_autoban_count";
        public static final String COMPLAIN_AUTOBAN_TIME = "virtualserver_complain_autoban_time";
        public static final String COMPLAIN_AUTOBAN_REMOVE_TIME = "virtualserver_complain_remove_time";

        public static final String FORCED_SILENCE_THRESHOLD = "virtualserver_min_clients_in_channel_before_forced_silence";
        public static final String PRIORITY_SPEAKER_DIMMING_DB = "virtualserver_priority_speaker_dimm_modificator";

        public static final String FLAG_PASSWORD = "virtualserver_flag_password";

        public static final String ANTIFLOOD_TICK_REDUCTION = "virtualserver_antiflood_points_tick_reduce";
        public static final String ANTIFLOOD_BLOCK_THRESHOLD = "virtualserver_antiflood_points_needed_command_block";
        public static final String ANTIFLOOD_BLOCK_THRESHOLD_PER_IP = "virtualserver_antiflood_points_needed_ip_block";
        public static final String ANTIFLOOD_BLOCK_THRESHOLD_PLUGIN = "virtualserver_antiflood_points_needed_plugin_block";

        public static final String CLIENT_CONNECTIONS = "virtualserver_client_connections";
        public static final String QUERY_CONNECTIONS = "virtualserver_query_client_connections";
        public static final String QUERY_CONNECTIONS_ONLINE = "virtualserver_queryclientsonline";

        public static final String LOG_CLIENT = "virtualserver_log_client";
        public static final String LOG_QUERY = "virtualserver_log_query";
        public static final String LOG_CHANNEL = "virtualserver_log_channel";
        public static final String LOG_PERMISSIONS = "virtualserver_log_permissions";
        public static final String LOG_SERVER = "virtualserver_log_server";
        public static final String LOG_FILETRANSFER = "virtualserver_log_filetransfer";

        public static final String PACKETLOSS_SPEECH_TOTAL = "virtualserver_total_packetloss_speech";
        public static final String PACKETLOSS_KEEPALIVE = "virtualserver_total_packetloss_keepalive";
        public static final String PACKETLOSS_CONTROL = "virtualserver_total_packetloss_control";
        public static final String PACKETLOSS_TOTAL = "virtualserver_total_packetloss_total";
        public static final String PING = "virtualserver_total_ping";

        public abstract static class Connection {

            private Connection() {
            }

            public static final String FILETRANSFER_BANDWIDTH_SENT = "connection_filetransfer_bandwidth_sent";
            public static final String FILETRANSFER_BANDWIDTH_RECEIVED = "connection_filetransfer_bandwidth_received";
            public static final String FILETRANSFER_BYTES_SENT = "connection_filetransfer_bytes_sent_total";
            public static final String FILETRANSFER_BYTES_RECEIVED = "connection_filetransfer_bytes_received_total";
            public static final String SPEECH_SENT_PACKETS = "connection_packets_sent_speech";
            public static final String SPEECH_SENT_BYTES = "connection_bytes_sent_speech";
            public static final String KEEPALIVE_SENT_BYTES = "connection_bytes_sent_keepalive";
            public static final String KEEPALIVE_SENT_PACKETS = "connection_packets_sent_keepalive";
            public static final String KEEPALIVE_RECEIVED_BYTES = "connection_bytes_received_keepalive";
            public static final String KEEPALIVE_RECEIVED_PACKETS = "connection_packets_received_keepalive";
            public static final String CONTROL_SENT_PACKETS = "connection_packets_sent_control";
            public static final String CONTROL_SENT_BYTES = "connection_bytes_sent_control";
            public static final String CONTROL_RECEIVED_PACKETS = "connection_packets_received_control";
            public static final String CONTROL_RECEIVED_BYTES = "connection_bytes_received_control";

            public static final String SENT_PACKETS_TOTAL = "connection_packets_sent_total";
            public static final String SENT_BYTES_TOTAL = "connection_bytes_sent_total";
            public static final String RECEIVED_PACKETS_TOTAL = "connection_packets_received_total";
            public static final String RECEIVED_BYTES_TOTAL = "connection_bytes_received_total";

            public static final String BANDWIDTH_SENT_LAST_SECOND = "connection_bandwidth_sent_last_second_total";
            public static final String BANDWIDTH_SENT_LAST_MINUTE = "connection_bandwidth_sent_last_minute_total";
            public static final String BANDWIDTH_RECEIVED_LAST_SECOND = "connection_bandwidth_received_last_second_total";
            public static final String BANDWIDTH_RECEIVED_LAST_MINUTE = "connection_bandwidth_received_last_minute_total";
        }
    }
}
