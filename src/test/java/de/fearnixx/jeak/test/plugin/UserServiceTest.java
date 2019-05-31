package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JeakBotPlugin(id = "usersvctest")
public class UserServiceTest extends AbstractTestPlugin {

    private static final String LOOKUP_NICKNAME = "MarkL4YG";
    private static final String LOOKUP_UNIQUE_ID = "NKLz7mUMAqrv07j1CZJ5OcDfj6I=";
    private static final String LOOKUP_NICKNAME_OFF = "[Testificate] Mark";
    private static final String LOOKUP_UNIQUE_ID_OFF = "GANC6dTbew+a3A2h/8c5CGJXzsE=";

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Inject
    private IUserService userService;

    @Inject
    private IDataCache dataCache;

    public UserServiceTest() {
        addTest("lookup_nickname");
        addTest("lookup_uniqueId");
        addTest("lookup_nickname_offline");
        addTest("lookup_uniqueId_offline");
        addTest("lookup_live_offline_equals");
    }

    @Listener(order = Listener.Orders.LATER)
    public void onClientRefresh(IQueryEvent.IDataEvent.IRefreshClients event) {
        List<IClient> clientResults = userService.findClientByUniqueID(LOOKUP_UNIQUE_ID);
        List<IClient> clientNickResults = userService.findClientByNickname(LOOKUP_NICKNAME);
        List<IUser> userResults = userService.findUserByUniqueID(LOOKUP_UNIQUE_ID);

        if (!clientResults.isEmpty()) {
            success("lookup_uniqueId");
        } else {
            logger.warn("Lookup for online unique ID failed!");
        }
        if (!clientNickResults.isEmpty() && clientNickResults.stream().anyMatch(clientResults::contains)) {
            success("lookup_nickname");
        } else {
            logger.warn("Lookup for online nickname failed!");
        }
        if (clientResults.equals(userResults)) {
            success("lookup_live_offline_equals");
        } else {
            logger.warn("Comparision for offline & online lookup failed!");
        }

        List<IUser> offlineResults = userService.findUserByUniqueID(LOOKUP_UNIQUE_ID_OFF);
        List<IUser> offlineNicknameResults = userService.findUserByNickname(LOOKUP_NICKNAME_OFF);
        if (!offlineResults.isEmpty()) {
            success("lookup_uniqueId_offline");
        } else {
            logger.warn("Lookup for offline unique ID failed!");
        }
        if (!offlineNicknameResults.isEmpty()) {
            success("lookup_nickname_offline");
        } else {
            logger.warn("Lookup for offline nickname failed!");
        }
    }
}
