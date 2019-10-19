package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Listener;

@FrameworkService(serviceInterface = ICommandService.class)
public class TypedCommandService extends CommandService {

    @Override
    @Listener
    public void onTextMessage(IQueryEvent.INotification.ITextMessage event) {
        if (event.getMessage().startsWith("!")) {
            triggerCommand(event);
        }
    }

    private void triggerCommand(IQueryEvent.INotification.ITextMessage txtEvent) {

    }
}
