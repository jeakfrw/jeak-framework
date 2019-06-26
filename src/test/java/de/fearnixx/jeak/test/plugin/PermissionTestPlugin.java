package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JeakBotPlugin(id = "permtest")
public class PermissionTestPlugin extends AbstractTestPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PermissionTestPlugin.class);

//    private static final String LOOKUP_NICKNAME = "MarkL4YG";
    private static final String LOOKUP_UNIQUE_ID = "NKLz7mUMAqrv07j1CZJ5OcDfj6I=";
//    private static final String LOOKUP_NICKNAME_OFF = "[Testificate] Mark";
    private static final String LOOKUP_UNIQUE_ID_OFF = "GANC6dTbew+a3A2h/8c5CGJXzsE=";

    @Inject
    private IUserService userService;

    public PermissionTestPlugin() {
        addTest("offline_not_permitted");
        addTest("offline_set_perm_permitted");
        addTest("offline_del_perm_not_permitted");
        addTest("online_not_permitted");
        addTest("online_set_perm_permitted");
        addTest("online_del_perm_not_permitted");

        addTest("parent_create");
        addTest("parent_not_permitted");
        addTest("parent_set_perm_permitted");

        addTest("offline_add_parent");
        addTest("offline_has_parent");
        addTest("offline_transitive_has_perm");
        addTest("offline_del_parent");
        addTest("offline_not_has_parent");
        addTest("online_add_parent");
        addTest("online_has_parent");
        addTest("online_transitive_has_perm");
        addTest("online_del_parent");
        addTest("online_not_has_parent");

        addTest("user_has_perm_by_ts3");
        addTest("parent_delete");
    }

    @Listener
    public void onClientsRefreshed(IQueryEvent.IDataEvent.IRefreshClients event) {
        checkOfflineUser();
        checkOnlineUser();
    }

    private void checkOnlineUser() {
        List<IClient> online = userService.findClientByUniqueID(LOOKUP_UNIQUE_ID);
        if (online.size() > 0) {
            IClient client = online.get(0);
            if (!client.hasPermission("permtest.test_permission")) {
                success("online_not_permitted");
            }

            client.setPermission("permtest.test_permission", 75);
            if (client.hasPermission("permtest.test_permission")) {
                success("online_set_perm_permitted");
            }

            client.removePermission("permtest.test_permission");
            if (!client.hasPermission("permtest.test_permission")) {
                success("online_del_perm_not_permitted");
            }
        } else {
            logger.warn("Online user was not found!");
        }
    }

    private void checkOfflineUser() {
        List<IUser> offline = userService.findUserByUniqueID(LOOKUP_UNIQUE_ID_OFF);
        if (offline.size() > 0) {
            IUser user = offline.get(0);
            if (!user.hasPermission("permtest.test_permission")) {
                success("offline_not_permitted");
            }

            user.setPermission("permtest.test_permission", 75);
            if (user.hasPermission("permtest.test_permission")) {
                success("offline_set_perm_permitted");
            }

            user.removePermission("permtest.test_permission");
            if (!user.hasPermission("permtest.test_permission")) {
                success("offline_del_perm_not_permitted");
            }

        } else {
            logger.warn("Offline user was not found!");
        }
    }
}
