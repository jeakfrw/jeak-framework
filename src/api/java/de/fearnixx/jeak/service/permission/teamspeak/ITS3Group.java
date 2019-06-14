package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.teamspeak.ITS3Subject;

import java.util.List;

/**
 * Representation of TS3 server or channel groups.
 */
public interface ITS3Group extends ITS3Subject {

    /**
     * @implNote  TS3 does not have a concept of groups inheriting from other groups. This will always be empty!
     */
    @Override
    List<IGroup> getParents();
}
