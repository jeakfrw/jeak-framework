package de.fearnixx.jeak.teamspeak;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionSID {

    private static Map<String, PermissionSID> INSTANCES = new HashMap<>();
    private static final Pattern PERMID_EXTR = Pattern.compile("^(?:i_|b_)(.+)$");

    public static final PermissionSID ALLOW_QUERY_LOGIN = of("b_serverquery_login");
    public static final PermissionSID ICON_ID = of("i_icon_id");
    public static final PermissionSID MAX_ICON_SIZE = of("i_max_icon_filesize");
    public static final PermissionSID PERMISSION_MODIFY_POWER = of("i_permission_modify_power");
    public static final PermissionSID PERMISSION_MODIFY_POWER_IGNORE = of("b_permission_modify_power_ignore");
    public static final PermissionSID CLIENT_PERMISSION_MODIFY_POWER = of("i_client_permission_modify_power");
    public static final PermissionSID CLIENT_NEEDED_PERMISSION_MODIFY_POWER = of("i_client_needed_permission_modify_power");
    public static final PermissionSID CLIENT_MAX_CLONES_PER_UID = of("i_client_max_clones_uid");
    public static final PermissionSID CLIENT_MAX_IDLE_TIME = of("i_client_max_idletime");
    public static final PermissionSID CLIENT_MAX_AVATAR_SIZE = of("i_client_max_avatar_filesize");
    public static final PermissionSID CLIENT_MAX_CHANNEL_SUBSCRIPTIONS = of("i_client_max_channel_subscriptions");
    public static final PermissionSID CLIENT_IS_PRIORITY_SPEAKER = of("b_client_is_priority_speaker");
    public static final PermissionSID CLIENT_SKIP_CHANNELGROUP_PERMISSIONS = of("b_client_skip_channelgroup_permissions");
    public static final PermissionSID CLIENT_FORCE_PUSH_TO_TALK = of("b_client_force_push_to_talk");
    public static final PermissionSID CLIENT_IGNORE_BANS = of("b_client_ignore_bans");
    public static final PermissionSID CLIENT_IGNORE_ANTIFLOOED = of("b_client_ignore_antiflood");
    public static final PermissionSID CLIENT_ALLOW_CLIENT_QUERY_COMMANDS = of("b_client_issue_client_query_command");
    public static final PermissionSID CLIENT_USE_RESERVED_SLOT = of("b_client_use_reserved_slot");
    public static final PermissionSID CLIENT_USE_CHANNEL_COMMANDER = of("b_client_use_channel_commander");
    public static final PermissionSID CLIENT_REQUEST_TALK_POWER = of("b_client_request_talker");
    public static final PermissionSID CLIENT_AVATAR_DELETE_OTHER = of("b_client_avatar_delete_other");
    public static final PermissionSID CLIENT_IS_STICKY = of("b_client_is_sticky");
    public static final PermissionSID CLIENT_IGNORE_STICKY = of("b_client_ignore_sticky");
    public static final PermissionSID CLIENT_INFO = of("b_client_info_view");
    public static final PermissionSID CLIENT_VIEW_PERMISSION_OVERVIEW = of("b_client_permissionoverview_view");
    public static final PermissionSID CLIENT_VIEW_PERMISSION_OVERVIEW_OWN = of("b_client_permissionoverview_own");
    public static final PermissionSID CLIENT_VIEW_REMOTE_ADDRESSES = of("b_client_remoteaddress_view");
    public static final PermissionSID CLIENT_VIEW_CUSTOMINFO = of("b_client_custom_info_view");
    public static final PermissionSID CLIENT_KICK_SERVER_POWER = of("i_client_kick_from_server_power");
    public static final PermissionSID CLIENT_NEEDED_KICK_SERVER_POWER = of("i_client_needed_kick_from_server_power");
    public static final PermissionSID CLIENT_KICK_CHANNEL_POWER = of("i_client_kick_from_channel_power");
    public static final PermissionSID CLIENT_NEEDED_KICK_CHANNEL_POWER = of("i_client_needed_kick_from_channel_power");
    public static final PermissionSID CLIENT_BAN_POWER = of("i_client_ban_power");
    public static final PermissionSID CLIENT_NEEDED_BAN_POWER = of("i_client_needed_ban_power");
    public static final PermissionSID CLIENT_MOVE_POWER = of("i_client_move_power");
    public static final PermissionSID CLIENT_NEEDED_MOVE_POWER = of("i_client_needed_move_power");
    public static final PermissionSID CLIENT_COMPLAIN_POWER = of("i_client_complain_power");
    public static final PermissionSID CLIENT_NEEDED_COMPLAIN_POWER = of("i_client_needed_complain_power");
    public static final PermissionSID CLIENT_COMPLAIN_LIST = of("b_client_complain_list");
    public static final PermissionSID CLIENT_COMPLAIN_DELETE_OWN = of("b_client_complain_delete_own");
    public static final PermissionSID CLIENT_COMPLAIN_DELETE = of("b_client_complain_delete");
    public static final PermissionSID CLIENT_BAN_LIST = of("b_client_ban_list");
    public static final PermissionSID CLIENT_BAN = of("b_client_ban_create");
    public static final PermissionSID CLIENT_BAN_DELETE_OWN = of("b_client_ban_delete_own");
    public static final PermissionSID CLIENT_BAN_DELETE = of("b_client_ban_delete");
    public static final PermissionSID CLIENT_BAN_MAX_TIME = of("i_client_ban_max_bantime");
    public static final PermissionSID CLIENT_TEXTMESSAGE_PRIVATE_POWER = of("i_client_private_textmessage_power");
    public static final PermissionSID CLIENT_NEEDED_TEXTMESSAGE_PRIVATE_POWER = of("i_client_needed_private_textmessage_power");
    public static final PermissionSID CLIENT_TEXTMESSAGE_SERVER = of("b_client_server_textmessage_send");
    public static final PermissionSID CLIENT_TEXTMESSAGE_CHANNEL = of("b_client_channel_textmessage_send");
    public static final PermissionSID CLIENT_TEXTMESSAGE_OFFLINE = of("b_client_offline_textmessage_send");
    public static final PermissionSID CLIENT_TALKPOWER = of("i_client_talk_power");
    public static final PermissionSID CLIENT_NEEDED_TALKPOWER = of("i_client_needed_talk_power");
    public static final PermissionSID CLIENT_POKE_POWER = of("i_client_poke_power");
    public static final PermissionSID CLIENT_NEEDED_POKE_POWER = of("i_client_needed_poke_power");
    public static final PermissionSID CLIENT_GRANT_TALKPOWER = of("b_client_set_flag_talker");
    public static final PermissionSID CLIENT_WHISPER_POWER = of("i_client_whisper_power");
    public static final PermissionSID CLIENT_NEEDED_WHISPER_POWER = of("i_client_needed_whisper_power");
    public static final PermissionSID CLIENT_MODIFY_DESCRIPTION = of("b_client_modify_description");
    public static final PermissionSID CLIENT_MODIFY_DESCRIPTION_OWN = of("b_client_modify_own_description");
    public static final PermissionSID CLIENT_MODIFY_DBPROPS = of("b_client_modify_dbproperties");
    public static final PermissionSID CLIENT_DELETE_DBPROPS = of("b_client_delete_dbproperties");
    public static final PermissionSID CLIENT_CREATE_QUERY_LOGIN = of("b_client_create_modify_serverquery_login");
    public static final PermissionSID GROUP_MODIFY_POWER = of("i_group_modify_power");
    public static final PermissionSID GROUP_NEEDED_MODIFY_POWER = of("i_group_needed_modify_power");
    public static final PermissionSID GROUP_MEMBER_ADD_POWER = of("i_group_member_add_power");
    public static final PermissionSID GROUP_NEEDED_MEMBER_ADD_POWER = of("i_group_needed_member_add_power");
    public static final PermissionSID GROUP_MEMBER_DEL_POWER = of("i_group_member_remove_power");
    public static final PermissionSID GROUP_NEEDED_MEMBER_DEL_POWER = of("i_group_needed_member_remove_power");
    public static final PermissionSID GROUP_IS_PERMANENT = of("b_group_is_permanent");
    public static final PermissionSID GROUP_AUTO_UPDATE_TYPE = of("i_group_auto_update_type");
    public static final PermissionSID GROUP_AUTO_UPDATE_MAX = of("i_group_auto_update_max_value");
    public static final PermissionSID GROUP_SORT_ID = of("i_group_sort_id");
    public static final PermissionSID GROUP_SHOW_IN_TREE = of("i_group_show_name_in_tree");
    public static final PermissionSID FILETRANSFER_IGNORE_PASSWORD = of("b_ft_ignore_password");
    public static final PermissionSID FILETRANSFER_LIST = of("b_ft_ignore_password");
    public static final PermissionSID FILETRANSFER_UPLOAD_POWER = of("i_ft_file_upload_power");
    public static final PermissionSID FILETRANSFER_NEEDED_UPLOAD_POWER = of("i_ft_needed_file_upload_power");
    public static final PermissionSID FILETRANSFER_DOWNLOAD_POWER = of("i_ft_file_download_power");
    public static final PermissionSID FILETRANSFER_NEEDED_DOWNLOAD_POWER = of("i_ft_needed_file_download_power");
    public static final PermissionSID FILETRANSFER_DELETE_POWER = of("i_ft_file_delete_power");
    public static final PermissionSID FILETRANSFER_NEEDED_DELETE_POWER = of("i_ft_needed_file_delete_power");
    public static final PermissionSID FILETRANSFER_RENAME_POWER = of("i_ft_file_rename_power");
    public static final PermissionSID FILETRANSFER_NEEDED_RENAME_POWER = of("i_ft_needed_file_rename_power");
    public static final PermissionSID FILETRANSFER_BROWSE_POWER = of("i_ft_file_browse_power");
    public static final PermissionSID FILETRANSFER_NEEDED_BROWSE_POWER = of("i_ft_needed_file_browse_power");
    public static final PermissionSID FILETRANSFER_MKDIR_POWER = of("i_ft_directory_create_power");
    public static final PermissionSID FILETRANSFER_NEEDED_MKDIR_POWER = of("i_ft_needed_directory_create_power");
    public static final PermissionSID FILETRANSFER_CLIENT_DOWNLOAD_QUOTA = of("i_ft_quota_mb_download_per_client");
    public static final PermissionSID FILETRANSFER_CLIENT_UPLOAD_QUOTA = of("i_ft_quota_mb_upload_per_client");
    public static final PermissionSID SERVER_CREATE = of("b_virtualserver_create");
    public static final PermissionSID SERVER_DELETE = of("b_virtualserver_delete");
    public static final PermissionSID SERVER_START_ANY = of("b_virtualserver_start_any");
    public static final PermissionSID SERVER_START_ONE = of("b_virtualserver_start");
    public static final PermissionSID SERVER_STOP_ANY = of("b_virtualserver_stop_any");
    public static final PermissionSID SERVER_STOP_ONE = of("b_virtualserver_stop");
    public static final PermissionSID SERVER_CHANGE_MACHINE_ID = of("b_virtualserver_change_machine_id");
    public static final PermissionSID SERVER_CHANGE_TEMPLATE = of("b_virtualserver_change_template");
    public static final PermissionSID SERVER_SELECT = of("b_virtualserver_select");
    public static final PermissionSID SERVER_INFO_VIEW = of("b_virtualserver_info_view");
    public static final PermissionSID SERVER_CONNECTION_INFO = of("b_virtualserver_connectioninfo_view");
    public static final PermissionSID SERVER_CHANNEL_LIST = of("b_virtualserver_channel_list");
    public static final PermissionSID SERVER_CHANNEL_SEARCH = of("b_virtualserver_channel_search");
    public static final PermissionSID SERVER_CLIENT_LIST = of("b_virtualserver_client_list");
    public static final PermissionSID SERVER_CLIENT_SEARCH = of("b_virtualserver_client_search");
    public static final PermissionSID SERVER_CLIENT_LIST_DB = of("b_virtualserver_client_dblist");
    public static final PermissionSID SERVER_CLIENT_SEARCH_DB = of("b_virtualserver_client_dbsearch");
    public static final PermissionSID SERVER_CLIENT_DB_INFO = of("b_virtualserver_client_dbinfo");
    public static final PermissionSID SERVER_PERMISSION_FIND = of("b_virtualserver_permission_find");
    public static final PermissionSID SERVER_CUSTOM_SEARCH = of("b_virtualserver_custom_search");
    public static final PermissionSID SERVER_TOKEN_LIST = of("b_virtualserver_token_list");
    public static final PermissionSID SERVER_TOKEN_ADD = of("b_virtualserver_token_add");
    public static final PermissionSID SERVER_TOKEN_USE = of("b_virtualserver_token_use");
    public static final PermissionSID SERVER_TOKEN_DELETE = of("b_virtualserver_token_delete");
    public static final PermissionSID SERVER_LOG_VIEW = of("b_virtualserver_log_view");
    public static final PermissionSID SERVER_LOG_ADD = of("b_virtualserver_log_add");
    public static final PermissionSID SERVER_IGNORE_JOIN_PASSWORD = of("b_virtualserver_join_ignore_password");
    public static final PermissionSID SERVER_NOTIFICATION_REGISTER = of("b_virtualserver_notify_register");
    public static final PermissionSID SERVER_NOTIFICATION_UNREGISTER = of("b_virtualserver_notify_unregister");
    public static final PermissionSID SERVER_SNAPSHOT_CREATE = of("b_virtualserver_snapshot_create");
    public static final PermissionSID SERVER_SNAPSHOT_DEPLOY = of("b_virtualserver_snapshot_deploy");
    public static final PermissionSID SERVER_RESET_PERMISSIONS = of("b_virtualserver_permission_reset");
    public static final PermissionSID SERVER_MODIFY_NAME = of("b_virtualserver_modify_name");
    public static final PermissionSID SERVER_MODIFY_WELCOME_MESSAGE = of("b_virtualserver_modify_welcomemessage");
    public static final PermissionSID SERVER_MODIFY_MAX_CLIENTS = of("b_virtualserver_modify_maxclients");
    public static final PermissionSID SERVER_MODIFY_RESERVED_SLOTS = of("b_virtualserver_modify_reserved_slots");
    public static final PermissionSID SERVER_MODIFY_PASSWORD = of("b_virtualserver_modify_password");
    public static final PermissionSID SERVER_MODIFY_DEFAULT_SERVERGROUP = of("b_virtualserver_modify_default_servergroup");
    public static final PermissionSID SERVER_MODIFY_DEFAULT_CHANNELGROUP = of("b_virtualserver_modify_channelgroup");
    public static final PermissionSID SERVER_MODIFY_DEFAULT_CHANNELADMINGROUP = of("b_virtualserver_modify_channeladmingroup");
    public static final PermissionSID SERVER_MODIFY_FORCED_SILENCE = of("b_virtualserver_modify_channel_forced_silence");
    public static final PermissionSID SERVER_MODIFY_COMPLAIN = of("b_virtualserver_modify_complain");
    public static final PermissionSID SERVER_MODIFY_ANTIFLOOD = of("b_virtualserver_modify_antiflood");
    public static final PermissionSID SERVER_MODIFY_FILETRANSFER_SETTINGS = of("b_virtualserver_modify_ft_settings");
    public static final PermissionSID SERVER_MODIFY_FILETRANSFER_QUOTAS = of("b_virtualserver_modify_ft_quotas");
    public static final PermissionSID SERVER_MODIFY_HOST_MESSAGE = of("b_virtualserver_modify_hostmessage");
    public static final PermissionSID SERVER_MODIFY_HOST_BANNER = of("b_virtualserver_modify_hostbanner");
    public static final PermissionSID SERVER_MODIFY_HOST_BUTTON = of("b_virtualserver_modify_hostbutton");
    public static final PermissionSID SERVER_MODIFY_PORT = of("b_virtualserver_modify_port");
    public static final PermissionSID SERVER_MODIFY_AUTOSTART = of("b_virtualserver_modify_autostart");
    public static final PermissionSID SERVER_MODIFY_ID_SECURITY_LEVEL = of("b_virtualserver_modify_needed_identity_security_level");
    public static final PermissionSID SERVER_MODIFY_PRIO_SPEAKER_DIMMING = of("b_virtualserver_modify_priority_speaker_dimm_modificator");
    public static final PermissionSID SERVER_MODIFY_LOG_SETTINGS = of("b_virtualserver_modify_log_settings");
    public static final PermissionSID SERVER_MODIFY_MIN_CLIENTVERSION = of("b_virtualserver_modify_min_client_version");
    public static final PermissionSID SERVER_MODIFY_ICON_ID = of("b_virtualserver_modify_icon_id");
    public static final PermissionSID SERVER_MODIFY_WEBLIST = of("b_virtualserver_modify_weblist");
    public static final PermissionSID SERVER_MODIFY_CODEC_ENCRYPTION = of("b_virtualserver_modify_codec_encryption_mode");
    public static final PermissionSID SERVER_MODIFY_TEMP_PASSWORDS = of("b_virtualserver_modify_temporary_passwords");
    public static final PermissionSID SERVER_MODIFY_TEMP_PASSWORDS_OWN = of("b_virtualserver_modify_temporary_passwords_own");
    public static final PermissionSID SERVER_LIST_SERVERGROUP = of("b_virtualserver_servergroup_list");
    public static final PermissionSID SERVER_LIST_SERVERGROUP_PERMISSIONS = of("b_virtualserver_servergroup_permission_list");
    public static final PermissionSID SERVER_LIST_SERVERGROUP_CLIENTS = of("b_virtualserver_servergroup_client_list");
    public static final PermissionSID SERVER_LIST_CHANNELGROUP = of("b_virtualserver_channelgroup_list");
    public static final PermissionSID SERVER_LIST_CHANNELGROUP_PERMISSIONS = of("b_virtualserver_channelgroup_permission_list");
    public static final PermissionSID SERVER_LIST_CHANNELGROUP_CLIENTS = of("b_virtualserver_channelgroup_client_list");
    public static final PermissionSID SERVER_LIST_CLIENT_PERMISSIONS = of("b_virtualserver_client_permission_list");
    public static final PermissionSID SERVER_LIST_CHANNEL_PERMISSIONS = of("b_virtualserver_channel_permission_list");
    public static final PermissionSID SERVER_LIST_CHANNEL_CLIENT_PERMISSIONS = of("b_virtualserver_channelclient_permission_list");
    public static final PermissionSID SERVER_CREATE_SERVERGROUP = of("b_virtualserver_servergroup_create");
    public static final PermissionSID SERVER_DELETE_SERVERGROUP = of("b_virtualserver_servergroup_delete");
    public static final PermissionSID SERVER_CREATE_CHANNELGROUP = of("b_virtualserver_channelgroup_create");
    public static final PermissionSID SERVER_DELETE_CHANNELGROUP = of("b_virtualserver_channelgroup_delete");
    public static final PermissionSID INSTANCE_HELP = of("b_serverinstance_help_view");
    public static final PermissionSID INSTANCE_VERSION = of("b_serverinstance_version_view");
    public static final PermissionSID INSTANCE_INFO_VIEW = of("b_serverinstance_info_view");
    public static final PermissionSID INSTANCE_LIST_VIRTUAL_SERVERS = of("b_serverinstance_virtualserver_list");
    public static final PermissionSID INSTANCE_LIST_BINDINGS = of("b_serverinstance_binding_list");
    public static final PermissionSID INSTANCE_LIST_PERMISSIONS = of("b_serverinstance_permission_list");
    public static final PermissionSID INSTANCE_FIND_PERMISSION = of("b_serverinstance_permission_find");
    public static final PermissionSID INSTANCE_SEND_GLOBAL_TEXTMESSAGE = of("b_serverinstance_textmessage_send");
    public static final PermissionSID INSTANCE_LOG_VIEW = of("b_serverinstance_log_view");
    public static final PermissionSID INSTANCE_LOG_ADD = of("b_serverinstance_log_add");
    public static final PermissionSID INSTANCE_STOP = of("b_serverinstance_stop");
    public static final PermissionSID INSTANCE_MODIFY_SETTINGS = of("b_serverinstance_modify_settings");
    public static final PermissionSID INSTANCE_MODIFY_QUERY_GROUP = of("b_serverinstance_modify_querygroup");
    public static final PermissionSID INSTANCE_MODIFY_TEMPLATES = of("b_serverinstance_modify_template");
    public static final PermissionSID CHANNEL_MIN_DEPTH = of("i_channel_min_depth");
    public static final PermissionSID CHANNEL_MAX_DEPTH = of("i_channel_max_depth");
    public static final PermissionSID CHANNEL_PERMISSION_MODIFY_POWER = of("i_channel_permission_modify_power");
    public static final PermissionSID CHANNEL_INFO_VIEW = of("b_channel_info_view");
    public static final PermissionSID CHANNEL_CREATE_CHILD = of("b_channel_create_child");
    public static final PermissionSID CHANNEL_CREATE_PERMANENT = of("b_channel_create_permanent");
    public static final PermissionSID CHANNEL_CREATE_SEMI_PERMANENT = of("b_channel_create_semi_permanent");
    public static final PermissionSID CHANNEL_CREATE_TEMPORARY = of("b_channel_create_temporary");
    public static final PermissionSID CHANNEL_CREATE_WITH_TOPIC = of("b_channel_create_with_topic");
    public static final PermissionSID CHANNEL_CREATE_WITH_DESCRIPTION = of("b_channel_create_with_description");
    public static final PermissionSID CHANNEL_CREATE_WITH_PASSWORD = of("b_channel_create_with_password");
    public static final PermissionSID CHANNEL_CREATE_CODEC_SPEEX8 = of("b_channel_create_modify_with_codec_speex8");
    public static final PermissionSID CHANNEL_CREATE_CODEC_SPEEX16 = of("b_channel_create_modify_with_codec_speex16");
    public static final PermissionSID CHANNEL_CREATE_CODEC_SPEEX32 = of("b_channel_create_modify_with_codec_speex32");
    public static final PermissionSID CHANNEL_CREATE_CODEC_CELTMONO48 = of("b_channel_create_modify_with_codec_celtmono48");
    public static final PermissionSID CHANNEL_CREATE_CODEC_CUSTOM_QUALITY = of("i_channel_create_modify_with_codec_maxquality");
    public static final PermissionSID CHANNEL_CREATE_CODEC_CUSTOM_LATENCY = of("i_channel_create_modify_with_codec_latency_factor_min");
    public static final PermissionSID CHANNEL_CREATE_WITH_MAXCLIENTS = of("b_channel_create_with_maxclients");
    public static final PermissionSID CHANNEL_CREATE_WITH_MAXFAMILYCLIENTS = of("b_channel_create_with_maxfamilyclients");
    public static final PermissionSID CHANNEL_CREATE_WITH_SORTORDER = of("b_channel_create_with_sortorder");
    public static final PermissionSID CHANNEL_CREATE_WITH_DEFAULT = of("b_channel_create_with_default");
    public static final PermissionSID CHANNEL_CREATE_WITH_TALKPOWER = of("b_channel_create_with_needed_talk_power");
    public static final PermissionSID CHANNEL_CREATE_FORCE_PASSWORD = of("b_channel_create_modify_with_force_password");
    public static final PermissionSID CHANNEL_MODIFY_PARENT = of("b_channel_modify_parent");
    public static final PermissionSID CHANNEL_MODIFY_MAKE_DEFAULT = of("b_channel_modify_make_default");
    public static final PermissionSID CHANNEL_MODIFY_MAKE_PERMANENT = of("b_channel_modify_make_permanent");
    public static final PermissionSID CHANNEL_MODIFY_MAKE_SEMI_PERMANENT = of("b_channel_modify_make_semi_permament");
    public static final PermissionSID CHANNEL_MODIFY_MAKE_TEMPORARY = of("b_channel_modify_make_temporary");
    public static final PermissionSID CHANNEL_MODIFY_NAME = of("b_channel_modify_name");
    public static final PermissionSID CHANNEL_MODIFY_TOPIC = of("b_channel_modify_topic");
    public static final PermissionSID CHANNEL_MODIFY_DESCRIPTION = of("b_channel_modify_description");
    public static final PermissionSID CHANNEL_MODIFY_PASSWORD = of("b_channel_modify_password");
    public static final PermissionSID CHANNEL_MODIFY_CODEC = of("b_channel_modify_codec");
    public static final PermissionSID CHANNEL_MODIFY_CODEC_QUALITY = of("b_channel_modify_codec_quality");
    public static final PermissionSID CHANNEL_MODIFY_CODEC_LATENCY = of("b_channel_modify_codec_latency_factor");
    public static final PermissionSID CHANNEL_MODIFY_MAXCLIENTS = of("b_channel_modify_maxclients");
    public static final PermissionSID CHANNEL_MODIFY_MAXFAMILYCLIENTS = of("b_channel_modify_maxfamilyclients");
    public static final PermissionSID CHANNEL_MODIFY_SORTORDER = of("b_channel_modify_sortorder");
    public static final PermissionSID CHANNEL_MODIFY_NEEDED_TALKPOWER = of("b_channel_modify_needed_talk_power");
    public static final PermissionSID CHANNEL_CHANNEL_MODIFY_POWER = of("i_channel_modify_power");
    public static final PermissionSID CHANNEL_NEEDED_MODIFY_POWER = of("i_channel_needed_modify_power");
    public static final PermissionSID CHANNEL_MODIFY_ENABLE_ENCRYPTION = of("b_channel_modify_make_codec_encrypted");
    public static final PermissionSID CHANNEL_DELETE_PERMANENT = of("b_channel_delete_permanent");
    public static final PermissionSID CHANNEL_DELETE_SEMI_PERMANENT = of("b_channel_delete_semi_permanent");
    public static final PermissionSID CHANNEL_DELETE_TEMPORARY = of("b_channel_delete_temporary");
    public static final PermissionSID CHANNEL_DELETE_FORCEFULLY = of("b_channel_delete_flag_force");
    public static final PermissionSID CHANNEL_DELETE_POWER = of("i_channel_delete_power");
    public static final PermissionSID CHANNEL_NEEDED_DELETE_POWER = of("i_channel_needed_delete_power");
    public static final PermissionSID CHANNEL_NEEDED_JOIN_POWER = of("i_channel_needed_join_power");
    public static final PermissionSID CHANNEL_NEEDED_SUBSCRIBE_POWER = of("i_channel_needed_subscribe_power");
    public static final PermissionSID CHANNEL_NEEDED_DESCRIPTION_VIEW_POWER = of("i_channel_needed_description_view_power");
    public static final PermissionSID CHANNEL_JOIN_PERMANENT = of("b_channel_join_permanent");
    public static final PermissionSID CHANNEL_JOIN_SEMI_PERMANENT = of("b_channel_join_semi_permanent");
    public static final PermissionSID CHANNEL_JOIN_TEMPORARY = of("b_channel_join_temporary");
    public static final PermissionSID CHANNEL_JOIN_POWER = of("i_channel_join_power");
    public static final PermissionSID CHANNEL_SUBSCRIBE_POWER = of("i_channel_subscribe_power");
    public static final PermissionSID CHANNEL_DESCRIPTION_VIEW_POWER = of("i_channel_description_view_power");
    public static final PermissionSID MANAGE_ICONS = of("b_icon_manage");
    public static final PermissionSID END_INHERITANCE = of("b_channel_group_inheritance_end");
    public static final PermissionSID SERVERQUERY_VIEW_POWER = of("i_client_serverquery_view_power");
    public static final PermissionSID SERVERQUERY_NEEDED_VIEW_POWER = of("i_client_needed_serverquery_view_power");

    public static Optional<PermissionSID> getSID(String permSID) {
        return Optional.ofNullable(INSTANCES.getOrDefault(permSID, null));
    }

    private static PermissionSID of(String permSID) {
        PermissionSID perm = new PermissionSID(permSID);
        INSTANCES.put(permSID, perm);
        return perm;
    }

    private final String permSID;

    private PermissionSID(String permSID) {
        this.permSID = permSID;
    }

    public String getPermSID() {
        return permSID;
    }

    /**
     * Also referred to as the "grant" permission.
     */
    public String getNeededModifySID() {
        Matcher matcher = PERMID_EXTR.matcher(permSID);
        if (!matcher.matches()) {
            throw new IllegalStateException("Protected PermSID");
        }

        return String.format("i_needed_modify_power_%s", matcher.group(1));
    }
}

