package de.fearnixx.jeak.service.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.util.IsolationLevel;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class HHPersistenceUnit extends Configurable implements IPersistenceUnit, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(HHPersistenceUnit.class);
    private static final String DEFAULT_DATASOURCE_CONFIG = "/database/defaultDS.json";

    private static final Map<String, Method> HIKARI_CONF_SETTER = new HashMap<>();

    static {
        Arrays.stream(HikariConfig.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("set"))
                .forEach(m -> HIKARI_CONF_SETTER.put(m.getName(), m));
    }

    private final Map<String, String> dataSourceOpts = new HashMap<>();
    private final List<EntityManager> entityManagers = new LinkedList<>();
    private final IConfig config;
    private final BootstrapServiceRegistry baseRegistry;
    private final String unitId;

    private boolean isClosed = false;
    private String jdbcUrl;
    private String host;
    private String port;
    private String schemaName;
    private String username;
    private String password;
    private String driver;

    private HikariDataSource hikariDS;
    private StandardServiceRegistry hibernateServiceRegistry;
    private SessionFactory hibernateSessionFactory;

    public HHPersistenceUnit(String unitId, IConfig config, BootstrapServiceRegistry baseRegistry) {
        super(HHPersistenceUnit.class);
        this.unitId = unitId;
        this.config = config;
        this.baseRegistry = baseRegistry;
    }

    @Override
    protected IConfig getConfigRef() {
        return config;
    }

    @Override
    protected String getDefaultResource() {
        return DEFAULT_DATASOURCE_CONFIG;
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        return false;
    }

    public boolean initialize() {
        if (hikariDS != null) {
            throw new IllegalStateException("Cannot re-initialize data source!");
        }

        if (loadConfig()) {
            readConfiguration();
            return initializeHikariSource() && initializeHibernateServices();
        } else {
            return false;
        }
    }

    private void readConfiguration() {
        host = getConfig().getNode("host").optString("localhost");
        port = getConfig().getNode("port").optString("3306");
        schemaName = getConfig().getNode("schema").optString("jeakbot");
        username = getConfig().getNode("user").optString("jeakbot-user");
        password = getConfig().getNode("pass").optString("secret");
        driver = getConfig().getNode("driver").optString("mariadb");
        jdbcUrl = getConfig().getNode("url").optString()
                .orElseGet(() -> String.format("jdbc:%s://%s:%s/%s", driver, host, port, schemaName));

        dataSourceOpts.put("maximumPoolSize", "4");
        dataSourceOpts.put("connectionTimeout", "240000");
        dataSourceOpts.put("transactionIsolation", IsolationLevel.TRANSACTION_REPEATABLE_READ.name());
        getConfig().getNode("dataSourceOpts")
                .optMap()
                .ifPresent(map ->
                        map.forEach((key, value) -> {
                            Optional<String> optVal = value.optString();
                            if (optVal.isEmpty()) {
                                logger.error("Cannot set DS property that is not a string! {} => {}", unitId, key);
                            } else {
                                dataSourceOpts.put(key, value.asString());
                            }
                        }));
    }

    private boolean initializeHikariSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrl());
        hikariConfig.setUsername(getUsername());
        hikariConfig.setPassword(getPassword());

        try {
            dataSourceOpts.forEach((key, value) -> hardSetDSProperty(key, value, hikariConfig));
            hikariDS = new HikariDataSource(hikariConfig);
            return true;
        } catch (HikariPool.PoolInitializationException e) {
            logger.error("Could not create persistence unit: {}", unitId, e);
            return false;
        }
    }

    @SuppressWarnings("java:S1193")
    private void hardSetDSProperty(String key, String value, HikariConfig hikariConfig) {
        String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        Method setter = HIKARI_CONF_SETTER.getOrDefault(methodName, null);
        if (setter == null) {
            NoSuchMethodException iA = new NoSuchMethodException("Hikari does not support setting the dataSource property: " + key);
            throw new HikariPool.PoolInitializationException(iA);
        }

        try {
            Class<?> paramType = setter.getParameterTypes()[0];
            Object param = null;

            if (paramType.isAssignableFrom(Integer.class) || paramType.equals(int.class)) {
                param = Integer.parseInt(value);
            } else if (paramType.isAssignableFrom(Long.class) || paramType.equals(long.class)) {
                param = Long.parseLong(value);
            } else if (paramType.isAssignableFrom(String.class)) {
                param = value;
            } else if (paramType.isAssignableFrom(Boolean.class) || paramType.equals(boolean.class)) {
                param = "true".equalsIgnoreCase(value) || "1".equals(value);
            }

            logger.info("Setting bean property \"{}\" -> \"{}\" on HikariConfig of \"{}\"", key, value, unitId);
            setter.invoke(hikariConfig, param);
            hikariConfig.addDataSourceProperty(key, value);
        } catch (IllegalAccessException | InvocationTargetException | NumberFormatException e) {
            Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            throw new HikariPool.PoolInitializationException(cause);
        }
    }

    private boolean initializeHibernateServices() {
        try {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder(baseRegistry);
            registryBuilder.applySetting("hibernate.format_sql", "true");
            registryBuilder.applySetting("hibernate.connection.datasource", hikariDS);

            hibernateServiceRegistry = registryBuilder.build();
            MetadataSources metaSources = new MetadataSources(hibernateServiceRegistry);
            for (Class<?> aClass : DatabaseService.getClasses()) {
                metaSources.addAnnotatedClassName(aClass.getName());
            }
            hibernateSessionFactory = metaSources.getMetadataBuilder().build().buildSessionFactory();
            return true;
        } catch (HibernateException e) {
            logger.warn("Failed to initialize Hibernate for persistence unit: {}", unitId, e);
            return false;
        }
    }

    @Override
    public String getUnitId() {
        return unitId;
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public DataSource getDataSource() {
        return hikariDS;
    }

    @Override
    public EntityManager getEntityManager() {
        return track(hibernateSessionFactory.createEntityManager());
    }

    private EntityManager track(EntityManager e) {
        entityManagers.add(e);
        return e;
    }

    @Override
    public synchronized void close() throws Exception {
        if (isClosed) {
            throw new IOException("Persistence unit already closed!");
        }
        isClosed = true;
        logger.debug("[{}] Closing & flushing entity managers.", unitId);
        entityManagers.forEach(eM -> {
            try {
                if (eM.isOpen()) {
                    if (eM.getTransaction().isActive()) {
                        eM.flush();
                    }
                    eM.close();
                }
            } catch (IllegalStateException | PersistenceException e) {
                logger.warn("[{}] Failed to close entity manager.", unitId, e);
            }
        });
        try {
            logger.debug("[{}] Closing hibernate session factory.", unitId);
            hibernateSessionFactory.close();
        } catch (IllegalArgumentException | PersistenceException e) {
            logger.warn("[{}] Failed to close Hibernate session factory.", unitId, e);
        }

        logger.debug("[{}] Closing Hibernate service registry.", unitId);
        StandardServiceRegistryBuilder.destroy(hibernateServiceRegistry);

        logger.debug("[{}] Closing Hikari source.", unitId);
        hikariDS.close();
    }
}
