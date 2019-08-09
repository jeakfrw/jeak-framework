package de.fearnixx.jeak.reflect;

import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.api.except.ParseException;
import de.mlessmann.confort.config.FileConfig;
import de.mlessmann.confort.lang.json.JSONConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigProvider extends AbstractSpecialProvider<Config> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

    private final File confDir;

    public ConfigProvider(File confDir) {
        this.confDir = confDir;
    }

    @Override
    public Class<Config> getAnnotationClass() {
        return Config.class;
    }

    public Optional<Object> provideWith(InjectionContext ctx, Field field) {
        final Config annotation = field.getAnnotation(Config.class);
        final Class<?> clazz = field.getType();

        Object value = null;
        String ctxId = ctx.getContextId();

        String fileName = annotation.id();
        if (fileName.isEmpty()) {
            fileName = ctxId;
        }

        if (fileName == null) {
            final String fieldName = field.getName();
            final String clazzName = field.getDeclaringClass().getName();
            logger.warn("Fallback configuration name used for {} in {}", fieldName, clazzName);
            fileName = clazzName + '.' + fieldName;
            fileName = fileName.replaceAll("(?i)loader", "");
            fileName = fileName.replaceAll("(?i)config", "");
        }

        File baseDir = confDir;
        if (ctxId != null) {
            baseDir = new File(baseDir, ctxId);
        }

        if (!annotation.category().isEmpty()) {
            baseDir = new File(baseDir, annotation.category());
        }

        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            final IOException except = new IOException("Failed to create target directory: " + baseDir.getPath());
            throw new UncheckedIOException(except);
        }

        File configFile = new File(baseDir, fileName + ".json");

        if (clazz.isAssignableFrom(IConfig.class)) {
            value = new FileConfig(new JSONConfigLoader(), configFile);

        } else if (clazz.isAssignableFrom(IConfigNode.class)) {
            logger.warn("DEPRECATION in {}: Injecting ConfigNode(s) is deprecated! Use de.mlessmann.confort.api.IConfig instead.", ctxId);
            IConfig config = new FileConfig(new JSONConfigLoader(), configFile);
            try {
                config.load();
                value = config.getRoot();
            } catch (IOException | ParseException e) {
                logger.error("Failed to load configuration for {}. Cannot inject.", ctxId, e);
                return Optional.empty();
            }

        } else if (clazz.isAssignableFrom(File.class)) {
            value = configFile;

        } else if (clazz.isAssignableFrom(Path.class)) {
            value = configFile.toPath();

        }

        return Optional.ofNullable(value);
    }
}
