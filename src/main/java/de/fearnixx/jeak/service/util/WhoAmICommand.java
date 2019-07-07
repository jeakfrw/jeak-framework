package de.fearnixx.jeak.service.util;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandContext;
import de.fearnixx.jeak.service.command.ICommandReceiver;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Map;
import java.util.Optional;

public class WhoAmICommand implements ICommandReceiver {

    @Inject
    private IUserService userService;

    @Override
    public void receive(ICommandContext ctx) {
        final Integer invokerId = ctx.getRawEvent().getInvokerId();
        final Optional<IClient> optClient = userService.getClientByID(invokerId);
        if (optClient.isPresent()) {
            final IClient client = optClient.get();

            final StringBuilder message = new StringBuilder();
            message.append("Your client information is: \n");
            final Map<String, String> props = client.getValues();
            props.forEach((k, v) -> message.append(k).append('=').append(v).append('\n'));
            ctx.getRawEvent().getConnection()
                    .sendRequest(client.sendMessage(message.toString()));
        }
    }
}
