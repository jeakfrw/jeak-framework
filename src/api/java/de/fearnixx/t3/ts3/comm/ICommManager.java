package de.fearnixx.t3.ts3.comm;

import de.fearnixx.t3.ts3.keys.TargetType;

import java.util.Optional;

/**
 * Created by Life4YourGames on 12.07.17.
 */
public interface ICommManager {

    /**
     * Open or retrieve a comm channel
     * @param type TargetType
     * @param id In case of client or channel - the id
     * @return The comm channel
     */
    Optional<ICommChannel> getCommChannel(TargetType type, Integer id);
}
