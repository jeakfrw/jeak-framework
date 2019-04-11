package de.fearnixx.jeak.teamspeak;

/**
 * All commands defined in the TS3 server query manual.
 */
public abstract class QueryCommands {

    private QueryCommands() {
    }

    /**
     * Server related commands.
     * Some commands have been put into a more specific category such as Permissions.
     */
    public abstract static class SERVER {
        private SERVER() {
        }

        public static final String HELP = "help";
        public static final String QUIT = "quit";
        public static final String LOGIN = "login";
        public static final String LOGOUT = "logout";
        public static final String VERSION = "version";
        public static final String HOST_INFO = "hostinfo";
        public static final String INSTANCE_INFO = "instanceinfo";
        public static final String INSTANCE_EDIT = "instanceedit";
        public static final String LIST_BINDINGS = "bindinglist";

        public static final String USE_INSTANCE = "use";
        public static final String SERVER_LIST = "serverlist";
        public static final String SERVER_GETIP_BYPORT = "serveridgetbyport";
        public static final String SERVER_CREATE = "servercreate";
        public static final String SERVER_DELETE = "serverdelete";
        public static final String SERVER_START = "serverstart";
        public static final String SERVER_STOP = "serverstop";
        public static final String SERVER_STOP_PROCESS = "serverprocessstop";
        public static final String SERVER_EDIT = "serveredit";
        public static final String SERVER_SNAPSHOT_CREATE = "serversnapshotcreate";
        public static final String SERVER_SNAPSHOT_DEPLOY = "serversnapshotdeploy";
        public static final String SERVER_INFO = "serverinfo";
        public static final String SERVER_REQUEST_CONNECTINFO = "serverrequestconnectioninfo";

        public static final String SERVER_NOTIFY_REGISTER = "servernotifyregister";
        public static final String SERVER_NOTIFY_UNREGISTER = "servernotifyunregister";
    }

    /**
     * Server group commands.
     * Some commands have been put into a more specific category such as Permissions.
     */
    public abstract static class SERVER_GROUP {
        private SERVER_GROUP() {
        }

        public static final String SERVERGROUP_LIST = "servergrouplist";
        public static final String SERVERGROUP_ADD = "servergroupadd";
        public static final String SERVERGROUP_DEL = "servergroupdel";
        public static final String SERVERGROUP_COPY = "servergroupcopy";
        public static final String SERVERGROUP_RENAME = "servergrouprename";
        public static final String SERVERGROUP_ADD_CLIENT = "servergroupaddclient";
        public static final String SERVERGROUP_DEL_CLIENT = "servergroupdelclient";
        public static final String SERVERGROUP_LIST_CLIENTS = "servergroupclientlist";
        public static final String SERVERGROUP_GET_BYCLIENT = "servergroupsbyclientid";
    }

    /**
     * Commands for remote logging.
     */
    public abstract static class LOG {
        private LOG() {
        }

        public static final String LOG_VIEW = "logview";
        public static final String LOG_ADD = "logadd";
    }

    /**
     * Commands for handling privilege keys.
     */
    public abstract static class PRIVILEGE_KEY {
        private PRIVILEGE_KEY() {
        }

        public static final String PRIVKEY_LIST = "privilegekeylist";
        public static final String PRIVKEY_ADD = "privilegekeyadd";
        public static final String PRIVKEY_DEL = "privilegekeydelete";
        public static final String PRIVKEY_USE = "privilegekeyuse";
    }

    /**
     * Permission commands grouped from the other categories.
     */
    public abstract static class PERMISSION {

        private PERMISSION() {
        }

        public static final String SERVERGROUP_LIST_PERMISSIONS = "servergrouppermlist";
        public static final String SERVERGROUP_ADD_PERMISSION = "servergroupaddperm";
        public static final String SERVERGROUP_ADD_PERMISSION_AUTO = "servergroupautoaddperm";
        public static final String SERVERGROUP_DEL_PERMISSION = "servergroupdelperm";
        public static final String SERVERGROUP_DEL_PERMISSION_AUTO = "servergroupautodelperm";

        public static final String CHANNEL_LIST_PERMISSIONS = "channelpermlist";
        public static final String CHANNEL_PERMISSION_ADD = "channeladdperm";
        public static final String CHANNEL_PERMISSION_DEL = "channeldelperm";

        public static final String CHANNEL_CLIENT_LIST_PERMISSIONS = "channelclientpermlist";
        public static final String CHANNEL_CLIENT_PERMISSION_ADD = "channelclientaddperm";
        public static final String CHANNEL_CLIENT_PERMISSION_DEL = "channelclientdelperm";

        public static final String CHANNEL_GROUP_PERMISSION_ADD = "channelgroupaddperm";
        public static final String CHANNEL_GROUP_PERMISSION_DEL = "channelgroupdelperm";
        public static final String CHANNEL_GROUP_PERMISSION_LIST= "channelgrouppermlist";

        public static final String CLIENT_LIST_PERMISSIONS = "clientpermlist";
        public static final String CLIENT_PERMISSION_ADD = "clientaddperm";
        public static final String CLIENT_PERMISSION_DEL = "clientdelperm";

        public static final String PERMISSION_LIST = "permissionlist";
        public static final String PERMISSION_GET_ID_BYNAME = "permidgetbyname";
        public static final String PERMISSION_OVERVIEW = "permoverview";
        public static final String PERMISSION_GET = "permget";
        public static final String PERMISSION_FIND = "permfind";
        public static final String PERMISSION_RESET = "permreset";
    }

    /**
     * Commands for managing offline messages.
     */
    public abstract static class MESSAGE {
        private MESSAGE() {
        }

        public static final String MESSAGE_LIST = "messagelist";
        public static final String MESSAGE_ADD = "messageadd";
        public static final String MESSAGE_DEL = "messagedel";
        public static final String MESSAGE_GET = "messageget";
        public static final String MESSAGE_UPDATE_FLAG = "messageupdateflag";
    }

    /**
     * Commands for managing complaints.
     */
    public abstract static class COMPLAINT {
        private COMPLAINT() {
        }

        public static final String COMPLAINT_LIST = "complainlist";
        public static final String COMPLAINT_ADD = "complainadd";
        public static final String COMPLAINT_DEL = "complaindel";
        public static final String COMPLAINT_DEL_ALL = "complaindelall";
    }

    /**
     * Commands for managing bans.
     */
    public abstract static class BAN {
        private BAN() {
        }

        public static final String BAN_CLIENT = "banclient";
        public static final String BAN_LIST = "banlist";
        public static final String BAN_ADD = "banadd";
        public static final String BAN_DEL = "bandel";
        public static final String BAN_DEL_ALL = "bandelall";
    }

    /**
     * Commands for managing channels.
     * Some commands have been put into a more specific category such as Permissions.
     */
    public abstract static class CHANNEL {
        private CHANNEL() {
        }

        public static final String CHANNEL_LIST = "channellist";
        public static final String CHANNEL_INFO = "channelinfo";
        public static final String CHANNEL_FIND = "channelfind";
        public static final String CHANNEL_MOVE = "channelmove";
        public static final String CHANNEL_CREATE = "channelcreate";
        public static final String CHANNEL_DELETE = "channeldelete";
        public static final String CHANNEL_EDIT = "channeledit";
    }

    /**
     * Channel group commands.
     * Some commands have been put into a more specific category such as Permissions.
     */
    public abstract static class CHANNEL_GROUP {
        private CHANNEL_GROUP() {
        }

        public static final String CHANNEL_GROUP_LIST = "channelgrouplist";
        public static final String CHANNEL_GROUP_ADD = "channelgroupadd";
        public static final String CHANNEL_GROUP_DEL = "channelgroupdel";
        public static final String CHANNEL_GROUP_COPY = "channelgroupcopy";
        public static final String CHANNEL_GROUP_RENAME = "channelgrouprename";
        public static final String CHANNEL_GROUP_LIST_CLIENTS = "channelgroupclientlist";
    }

    /**
     * Client related commands.
     * Some commands have been put into a more specific category such as Permissions.
     */
    public abstract static class CLIENT {
        private CLIENT() {
        }

        public static final String CLIENT_LIST = "clientlist";
        public static final String CLIENT_INFO = "clientinfo";
        public static final String CLIENT_FIND = "clientfind";

        // Updates a client //
        public static final String CLIENT_EDIT = "clientedit";
        // Updates the query client //
        public static final String CLIENT_UPDATE = "clientupdate";

        public static final String CLIENT_MOVE = "clientmove";
        public static final String CLIENT_KICK = "clientkick";
        public static final String CLIENT_POKE = "clientpoke";

        public static final String CLIENT_SET_CHANNEL_GROUP = "setclientchannelgroup";
        public static final String CLIENT_GET_IDS = "clientgetids";

        public static final String CLIENT_LIST_DB = "clientdblist";
        public static final String CLIENT_INFO_DB = "clientdbinfo";
        public static final String CLIENT_FIND_DB = "clientdbfind";
        public static final String CLIENT_EDIT_DB = "clientdbedit";
        public static final String CLIENT_DELETE_DB = "clientdbdelete";

        public static final String CLIENT_GET_DBID_FROMUID = "clientgetdbidfromuid";
        public static final String CLIENT_GET_NAME_FROMUID = "clientgetnamefromuid";
        public static final String CLIENT_GET_NAME_FROMDBID= "clientgetnamefromdbid";
    }

    /**
     * File transfer commands.
     * @deprecated File transfer is not yet supported! Deprecation will be removed once supported.
     */
    @Deprecated
    public abstract static class FILE_TRANSFER {
        private FILE_TRANSFER() {
        }

        public static final String FILE_TRANSFER_LIST = "ftlist";
        public static final String FILE_TRANSFER_LIST_FILES = "ftgetfilelist";
        public static final String FILE_TRANSFER_FILE_INFO = "ftgetfileinfo";
        public static final String FILE_TRANSFER_DELETE_FILE = "ftdeletefile";
        public static final String FILE_TRANSFER_RENAME_FILE = "ftrenamefile";
        public static final String FILE_TRANSFER_CREATE_DIR = "ftcreatedir";
        public static final String FILE_TRANSFER_INIT_UPLOAD = "ftinitupload";
        public static final String FILE_TRANSFER_INIT_DOWNLOAD = "ftinitdownload";
        public static final String FILE_TRANSFER_STOP = "ftstop";
    }

    public static final String TEXTMESSAGE_SEND = "sendtextmessage";
    public static final String CUSTOM_SEARCH = "customsearch";
    public static final String CUSTOM_INFO = "custominfo";
    public static final String WHOAMI = "whoami";
}
