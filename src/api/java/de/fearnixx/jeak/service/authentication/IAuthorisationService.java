package de.fearnixx.jeak.service.authentication;

import de.fearnixx.jeak.teamspeak.data.IUser;

/**
 * A Service for handling authentication of clients for resources.
 *
 */
public interface IAuthorisationService {
    String authorisationGrant(IUser user);
    String requestAccessToken(String authorizationGrant);
    boolean validateToken(String token);
}
