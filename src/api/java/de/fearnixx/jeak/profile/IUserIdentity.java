package de.fearnixx.jeak.profile;

import java.util.UUID;

/**
 * Association between a profile and a service identity.
 */
public interface IUserIdentity {

    String SERVICE_TEAMSPEAK = "teamspeak3";

    /**
     * The service this identity is associated with.
     * The framework only dictates TeamSpeak to use "teamspeak3".
     * For other identifiers, consider lookup up plugins that form profile connections for their available identifiers.
     */
    String serviceId();

    /**
     * The identity of that service.
     * With TeamSpeak, this will be the unique ID of the users identity. Other services may use different formats.
     * For example, Minecraft plugins could use {@link UUID#toString()}ed identifiers here.
     *
     * <p>This means, the format of the string is dependent on the service id!
     */
    String identity();
}
