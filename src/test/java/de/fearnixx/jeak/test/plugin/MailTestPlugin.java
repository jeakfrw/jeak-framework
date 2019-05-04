package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.mail.IAttachment;
import de.fearnixx.jeak.service.mail.IMail;
import de.fearnixx.jeak.service.mail.ITransportUnit;
import de.fearnixx.jeak.test.AbstractTestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@JeakBotPlugin(id = "mailtest")
public class MailTestPlugin extends AbstractTestPlugin {

    private static final String PROP_TEST_RECIPIENT = "jeak.test.mailTest.recipient";
    private static final String TEST_RECIPIENT = Main.getProperty(PROP_TEST_RECIPIENT, null);

    private static final Logger logger = LoggerFactory.getLogger(MailTestPlugin.class);

    @Inject
    @TransportUnit(name = "test")
    private ITransportUnit transportUnit;

    @Inject
    @Config(id = "attachmentDummy")
    private File dummyConf;

    public MailTestPlugin() {
        if (TEST_RECIPIENT != null) {
            addTest("send_mail");
            addTest("send_mail_attached");
        } else {
            logger.warn("Not running tests. Missing recipient. ({})", PROP_TEST_RECIPIENT);
        }
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        if (TEST_RECIPIENT != null) {
            IMail message = IMail.builder()
                    .addSubjectText("Yay! It works!")
                    .addText("This is a test-email!")
                    .to(TEST_RECIPIENT)
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
