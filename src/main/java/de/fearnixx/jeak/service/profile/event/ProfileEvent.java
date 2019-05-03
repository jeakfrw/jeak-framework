package de.fearnixx.jeak.service.profile.event;

import de.fearnixx.jeak.profile.IUserProfile;
import de.fearnixx.jeak.profile.event.IProfileEvent;

import java.util.UUID;

public abstract class ProfileEvent implements IProfileEvent {

    private IUserProfile targetProfile;

    public IUserProfile getTargetProfile() {
        return targetProfile;
    }

    public void setTargetProfile(IUserProfile targetProfile) {
        this.targetProfile = targetProfile;
    }

    public static class ProfileCreatedEvent extends ProfileEvent implements IProfileCreated {
    }

    public static class ProfileDeletedEvent extends ProfileEvent implements IProfileDeleted {

        private UUID profileUUID;

        @Override
        public UUID getProfileUUID() {
            return profileUUID;
        }

        public void setProfileUUID(UUID profileUUID) {
            this.profileUUID = profileUUID;
        }
    }

    public static class ProfileMergeEvent extends ProfileEvent implements IProfileMerge {

        private IUserProfile mergeSource;

        @Override
        public IUserProfile getMergeSource() {
            return mergeSource;
        }

        public void setMergeSource(IUserProfile mergeSource) {
            this.mergeSource = mergeSource;
        }
    }
}
