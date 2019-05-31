package de.fearnixx.jeak.service.token;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;

import java.util.Optional;

@FrameworkService(serviceInterface = ITokenService.class)
public class TokenService implements ITokenService {

    @Inject
    private IInjectionService injectionService;

    private TokenConfiguration tokenConfiguration;

    @Override
    public boolean verifyToken(Class<?> controllerClass, String token) {
        boolean isVerified = false;
        tokenConfiguration.readToken(controllerClass);
        Optional<String> configToken = tokenConfiguration.readToken(controllerClass);
        if (configToken.isPresent() && configToken.get().equals(token)) {
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
