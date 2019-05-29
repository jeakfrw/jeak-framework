package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.util.List;

/**
 * Service to provide access to online (client) and offline (user) instances for plugins to work with.
 * <p>
 * Note the following:
 * As TS3 does not enforce uniqueness, developers need to be aware of this multi-result scenario.
 * In fact, TS3 has been observed to end up with multiple entries of the same "unique" id in the database.
 * In addition, as {@link IClient} extends {@link IUser} the DBID-searching methods can also return multiple instances for each connection a client has.
 * This is why all search methods return lists.
 * </p>
 */
public interface IUserService {

    /**
     * Searches a user based on their unique ID.
     * @apiNote please note the class-level javadoc.
     */
    List<IUser> findUserByUniqueID(String ts3uniqueID);

    /**
     * Searches a user based on their database ID.
     * @apiNote please note the class-level javadoc.
     */
    List<IUser> findUserByDBID(int ts3dbID);

    List<IUser> findUserByNickname(String ts3nickname);

    List<IClient> findClientByUniqueID(String ts3uniqueID);

    List<IClient> findClientByDBID(int ts3dbID);

    List<IClient> findClientByNickname(String ts3nickname);
}
