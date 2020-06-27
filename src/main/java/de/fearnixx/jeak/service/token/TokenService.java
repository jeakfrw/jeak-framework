package de.fearnixx.jeak.service.token;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.except.InvokationBeforeInitializationException;

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

    @Override
    public boolean revokeToken(String token) {
        if (tokenConfiguration == null) {
            throw new InvokationBeforeInitializationException("The TokenConfiguration is not initialized");
        }
        return tokenConfiguration.deleteToken(token);
    }

    @Listener
    public void onPreInit(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        tokenConfiguration = new TokenConfiguration();
        injectionService.injectInto(tokenConfiguration);
        tokenConfiguration.loadConfig();
    }

    private String createToken() {
        RandomString tickets = new RandomString(30);
        return tickets.nextString();
    }
}
