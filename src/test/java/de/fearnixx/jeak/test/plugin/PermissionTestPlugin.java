package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionProvider;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.except.CircularInheritanceException;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@JeakBotPlugin(id = "permtest")
public class PermissionTestPlugin extends AbstractTestPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PermissionTestPlugin.class);

    //    private static final String LOOKUP_NICKNAME = "MarkL4YG";
    private static final String LOOKUP_UNIQUE_ID = "NKLz7mUMAqrv07j1CZJ5OcDfj6I=";
    //    private static final String LOOKUP_NICKNAME_OFF = "[Testificate] Mark";
    private static final String LOOKUP_UNIQUE_ID_OFF = "GANC6dTbew+a3A2h/8c5CGJXzsE=";

    @Inject
    private IUserService userService;

    @Inject
    private IPermissionService permissionService;

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
        addTest("parent_circularity_detect");
    }

    @Listener
    public void onClientsRefreshed(IQueryEvent.IDataEvent.IRefreshClients event) {
        checkOfflineUser();
        checkOnlineUser();
        testOfflineParents();
        testOnlineParents();
        testCircularityDetection();
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
        Optional<IUser> optUser = findOfflineUser();
        optUser.ifPresent(user -> {
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
        });
    }

    private void testOfflineParents() {
        IGroup group = permissionService.getFrameworkProvider().createParent("test-group").orElse(null);

        if (group != null) {
            success("parent_create");
            if (!group.hasPermission("permtest.test_permission")) {
                success("parent_not_permitted");
            }

            group.setPermission("permtest.test_permission", 75);
            if (group.hasPermission("permtest.test_permission")) {
                success("parent_set_perm_permitted");
            }

            Optional<IUser> optUser = findOfflineUser();
            optUser.ifPresent(user -> {
                if (group.addMember(user)) {
                    success("offline_add_parent");
                }

                if (user.hasParent(group.getUniqueID())) {
                    success("offline_has_parent");
                }

                group.setPermission("permtest.test_permission", 75);
                if (user.hasPermission("permtest.test_permission")) {
                    success("offline_transitive_has_perm");
                }

                if (permissionService.getFrameworkProvider().deleteSubject(group.getUniqueID())) {
                    success("offline_del_parent");

                    if (!user.hasParent(group.getUniqueID())) {
                        success("offline_not_has_parent");
                    } else {
                        logger.warn("Offline user still has deleted group as parent!");
                    }
                } else {
                    logger.warn("Failed to delete offline-parent!");
                }
            });
        }
    }

    private void testOnlineParents() {
        IGroup group = permissionService.getFrameworkProvider().createParent("test-group").orElse(null);

        if (group != null) {
            Optional<IClient> optClient = findOnlineClient();
            optClient.ifPresent(client -> {
                if (group.addMember(client)) {
                    success("online_add_parent");
                }

                if (client.hasParent(group.getUniqueID())) {
                    success("online_has_parent");
                }

                group.setPermission("permtest.test_permission", 75);
                if (client.hasPermission("permtest.test_permission")) {
                    success("online_transitive_has_perm");
                }

                if (permissionService.getFrameworkProvider().deleteSubject(group.getUniqueID())) {
                    success("online_del_parent");

                    if (!client.hasParent(group.getUniqueID())) {
                        success("online_not_has_parent");
                    } else {
                        logger.warn("Online client still has deleted group as parent!");
                    }
                } else {
                    logger.warn("Failed to delete online-parent!");
                }

                group.linkServerGroup(client.getGroupIDs().get(0));
                if (!client.hasParent(group.getUniqueID())) {
                    if (client.hasPermission("permtest.test_permission")) {
                        success("user_has_perm_by_ts3");
                    } else {
                        logger.warn("Client not picked up by group-linking!");
                    }
                } else {
                    logger.warn("Client still member of linked-test-group!");
                }

                if (!permissionService.getFrameworkProvider().deleteSubject(group.getUniqueID())) {
                    logger.warn("Failed to delete test subject group for online tests.");
                    fail("parent_delete");
                } else {
                    success("parent_delete");
                }
            });
        } else {
            logger.warn("Failed to create parent for online-client tests!");
            fail("parent_create");
        }
    }

    @SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
    private void testCircularityDetection() {
        final IPermissionProvider provider = permissionService.getFrameworkProvider();
        IGroup a = null;
        IGroup b = null;
        IGroup c = null;
        try {
            a = provider.createParent("group-A").get();
            b = provider.createParent("group-B").get();
            c = provider.createParent("group-C").get();
            a.addMember(b);
            b.addMember(c);

            // This should fail.
            c.addMember(a);

        } catch (CircularInheritanceException e) {
            success("parent_circularity_detect");
        } finally {
            provider.deleteSubject(a.getUniqueID());
            provider.deleteSubject(b.getUniqueID());
            provider.deleteSubject(c.getUniqueID());
        }
    }


    private Optional<IUser> findOfflineUser() {
        List<IUser> offline = userService.findUserByUniqueID(LOOKUP_UNIQUE_ID_OFF);
        if (offline.size() > 0) {
            return Optional.of(offline.get(0));
        } else {
            logger.warn("Offline user was not found!");
            return Optional.empty();
        }
    }

    private Optional<IClient> findOnlineClient() {
        List<IClient> online = userService.findClientByUniqueID(LOOKUP_UNIQUE_ID);
        if (online.size() > 0) {
            return Optional.of(online.get(0));
        } else {
            logger.warn("Online client was not found!");
            return Optional.empty();
        }
    }
}
