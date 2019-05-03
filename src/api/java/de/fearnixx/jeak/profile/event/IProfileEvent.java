package de.fearnixx.jeak.profile.event;

import de.fearnixx.jeak.event.IEvent;
import de.fearnixx.jeak.profile.IUserProfile;

import java.util.UUID;

public interface IProfileEvent extends IEvent {

    interface IProfileCreated extends IProfileEvent, ITargetProfile {
    }

    interface IProfileDeleted extends IProfileEvent {

        UUID getProfileUUID();
    }

    /**
     * The service will merge two profiles.
     * Allows listeners to this event to clean up any references they have to the source profile which will be deleted.
     */
    interface IProfileMerge extends IProfileEvent, ITargetProfile {

        IUserProfile getMergeSource();
    }
}
