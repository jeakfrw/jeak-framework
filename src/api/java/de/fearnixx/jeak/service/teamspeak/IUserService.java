package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.util.List;
import java.util.Optional;

/**
 * Service to provide access to online (client) and offline (user) instances for plugins to work with.
 * <p>
 * Note the following:
 * As TS3 does not enforce uniqueness, developers need to be aware of this multi-result scenario.
 * In fact, TS3 has been observed to end up with multiple entries of the same "unique" id in the database.
 * In addition, as {@link IClient} extends {@link IUser} the DBID-searching methods can also return multiple instances for each connection a client has.
 * This is why all search methods return lists.
 * </p>
 * <p>
 * For any operations returning offline representations, online representations {@link IClient} may be returned.
 * This is due to {@link IUser} being a super interface of {@link IClient}.
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

    /**
     * Searches a user based on their nickname.
     * @apiNote please note the class-level javadoc.
     * @implNote the nickname is searched using case-insensitive "contains".
     */
    List<IUser> findUserByNickname(String ts3nickname);

    /**
     * Searches a client based on their unique ID.
     * @apiNote please note the class-level javadoc.
     */
    List<IClient> findClientByUniqueID(String ts3uniqueID);

    /**
     * Searches a client based on their database ID.
     * @apiNote please note the class-level javadoc.
     */
    List<IClient> findClientByDBID(int ts3dbID);

    /**
     * Searches a client based on their nickname.
     * @apiNote please note the class-level javadoc.
     */
    List<IClient> findClientByNickname(String ts3nickname);

    /**
     * Returns a client by their client ID.
     * As the client ID is unique for each connection to the TS3 server, this can only be one client, if any.
     */
    Optional<IClient> getClientByID(int clientId);
}
