package de.fearnixx.jeak.service.token;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.controller.InvokationBeforeInitializationException;

import java.security.SecureRandom;
import java.util.Set;

@FrameworkService(serviceInterface = ITokenService.class)
public class TokenService implements ITokenService {

    @Inject
    private IInjectionService injectionService;

    private TokenConfiguration tokenConfiguration;

    @Override
    public boolean verifyToken(String endpoint, String token) {
        if (tokenConfiguration == null) {
            throw new InvokationBeforeInitializationException("The TokenConfiguration is not initialized");
        }
        boolean isVerified = false;
        TokenScope tokenScopes = tokenConfiguration.getTokenScopes(token);
        if (tokenScopes.isInScope(endpoint)) {
            isVerified = true;
        }
        return isVerified;
    }

    @Override
    public String generateToken(Set<String> endpointSet) {
        if (tokenConfiguration == null) {
            throw new InvokationBeforeInitializationException("The TokenConfiguration is not initialized");
        }
        String token = createToken();
        tokenConfiguration.saveToken(token, new TokenScope(endpointSet));
        return token;
    }

    @Listener
    public void onPreInit(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        tokenConfiguration = new TokenConfiguration();
        injectionService.injectInto(tokenConfiguration);
        tokenConfiguration.loadConfig();
    }

    private String createToken() {
        String symbols = RandomString.digits + RandomString.upper + RandomString.lower;
        RandomString tickets = new RandomString(23, new SecureRandom(), symbols);
        return tickets.nextString();
    }
}
