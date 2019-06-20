package de.fearnixx.jeak.service.permission.teamspeak;

import java.util.List;

public interface ITS3UserSubject extends ITS3Subject {

    Integer getClientDBID();

    /**
     * Specialized getter for TS3 server groups.
     * @return
     */
    List<ITS3ServerGroupSubject> getServerGroups();
}
