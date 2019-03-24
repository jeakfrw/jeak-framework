package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.mail.IAttachment;
import de.fearnixx.jeak.service.mail.IMail;
import de.fearnixx.jeak.service.mail.ITransportUnit;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@JeakBotPlugin(id = "mailtest")
public class MailTestPlugin extends AbstractTestPlugin {

    @Inject
    @TransportUnit(name = "test")
    private ITransportUnit transportUnit;

    @Inject
    @Config(id = "attachmentDummy")
    private File dummyConf;

    public MailTestPlugin() {
        addTest("send_mail");
        addTest("send_mail_attached");
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        IMail message = IMail.builder()
                .addSubjectText("Yay! It works!")
                .addText("This is a test-email!")
                .to("technik@fearnixx.de")
                .build();

        transportUnit.dispatch(message);
        success("send_mail");

        writeDummy();
        IMail attachedMessage = IMail.builder()
                .addSubjectText("Yay! This works too!")
                .addText("This example is for attachments.")
                .to("technik@fearnixx.de")
                .attach(IAttachment.builder().fromFile("MyAttachment.json", dummyConf))
                .build();
        transportUnit.dispatch(attachedMessage);
        success("send_mail_attached");
    }

    private void writeDummy() {
        try (FileWriter writer = new FileWriter(dummyConf)) {
            writer.write("{\"this\": \"is a dummy-configuration file\"}");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dummy configuration for attachment test!");
        }
    }
}
