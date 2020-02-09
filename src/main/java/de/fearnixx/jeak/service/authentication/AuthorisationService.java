package de.fearnixx.jeak.service.authentication;

import de.fearnixx.jeak.teamspeak.data.IUser;

public class AuthorisationService implements IAuthorisationService {
    private AuthorisationEntityManager authorisationEntityManager;

    @Override
    public String authorisationGrant(IUser user) {
        return null;
    }

    @Override
    public String requestAccessToken(String authorizationGrant) {
        return null;
    }

    @Override
    public boolean validateToken(String token) {
        return false;
    }
}
