package de.fearnixx.jeak.service.token;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;

@FrameworkService(serviceInterface = ITokenService.class)
public class TokenService implements ITokenService {

    @Inject
    private IInjectionService injectionService;

    private TokenConfiguration tokenConfiguration;

    @Override
    public boolean verifyToken(String endpoint, String token) {
        boolean isVerified = false;
        TokenScope tokenScopes = tokenConfiguration.getTokenScopes(token);
        if (tokenScopes.isInScope(endpoint)) {
            isVerified = true;
        }
        return isVerified;
    }

    @Listener
    public void onPreInit(IBotStateEvent.IPreInitializeEvent preInitializeEvent) {
        tokenConfiguration = new TokenConfiguration();
        injectionService.injectInto(tokenConfiguration);
        tokenConfiguration.loadConfig();
    }
}
