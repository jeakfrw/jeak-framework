package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.mail.IMail;
import de.fearnixx.jeak.service.mail.ITransportUnit;
import de.fearnixx.jeak.test.AbstractTestPlugin;

public class MailTestPlugin extends AbstractTestPlugin {

    @Inject
    private ITransportUnit transportUnit;

    public MailTestPlugin() {
        addTest("send_mail");
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        IMail message = IMail.builder()
                .addSubjectText("Yay! It works!")
                .addText("This is a test-email!")
                .to("technik@fearnixx.de")
                .build();

        transportUnit.dispatch(message);
    }
}
