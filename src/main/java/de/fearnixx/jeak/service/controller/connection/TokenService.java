package de.fearnixx.jeak.service.controller.connection;

import de.fearnixx.jeak.reflect.FrameworkService;

@FrameworkService(serviceInterface = ITokenService.class)
public class TokenService implements ITokenService {
    @Override
    public boolean verifyToken(String token) {
        return true;
    }
}
