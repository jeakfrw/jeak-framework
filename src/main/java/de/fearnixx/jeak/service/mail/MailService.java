package de.fearnixx.jeak.service.mail;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Listener;
import de.mlessmann.confort.LoaderFactory;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.api.lang.IConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailService implements IMailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private final String CONFIGURATION_MIME_TYPE = "application/json";

    private final ExecutorService dispatchExecutor = Executors.newSingleThreadExecutor();
    private Map<String, SmtpTransportUnit> transportUnits = new HashMap<>();
    private final File confDir;

    public MailService(File confDir) {
        this.confDir = confDir;
    }

    public void onLoad() {
        List<File> configCandidates = new LinkedList<>();

        if (confDir.isDirectory()) {
            logger.debug("Searching for SMTP configurations.");
            File[] files = confDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.canRead() && file.getName().endsWith(".json")) {
                        logger.debug("Found configuration candidate: {}", file.getName());
                        configCandidates.add(file);
                    }
                }
            }
        }

        IConfigLoader configLoader = LoaderFactory.getLoader(CONFIGURATION_MIME_TYPE);
        configCandidates.forEach(f -> this.loadUnit(f, configLoader));
    }

    private void loadUnit(File file, IConfigLoader configLoader) {
        try {
            IConfigNode config = configLoader.parse(file);
            String fileName = file.getName();
            String unitName = fileName.substring(0, fileName.lastIndexOf('.'));
            SmtpTransportUnit unit = new SmtpTransportUnit(unitName, dispatchExecutor);
            unit.load(config);
            transportUnits.put(unitName, unit);
            logger.info("Loaded transport-unit: {}", unitName);

        } catch (ParseException | IOException | MessagingException e) {
            logger.warn("Failed to load unit from: \"{}\"!", file.getPath(), e);
        }
    }

    public Optional<ITransportUnit> getTransportUnit(String unitName) {
        return Optional.ofNullable(transportUnits.getOrDefault(unitName, null));
    }
}
