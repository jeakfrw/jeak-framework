package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.database.IPersistenceUnit;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

@JeakBotPlugin(id = "hikari")
public class HikariTest extends AbstractTestPlugin {

    @Inject
    @PersistenceUnit(name = "test")
    private IPersistenceUnit persistenceUnit;

    @Inject
    @PersistenceUnit(name = "test")
    private DataSource dataSource;

    @Inject
    @PersistenceUnit(name = "test")
    private EntityManager entityManager;

    public HikariTest() {
        addTest("inject_unit");
        addTest("inject_dataSource");
        addTest("inject_entityManager");
    }

    @Listener
    public void onInitialize(IBotStateEvent.IInitializeEvent event) {
        if (persistenceUnit == null) {
            fail("inject_unit");
        } else {
            success("inject_unit");
        }

        if (dataSource == null) {
            fail("inject_dataSource");
        } else {
            success("inject_dataSource");
        }

        if (entityManager == null) {
            fail("inject_entityManager");
        } else {
            success("inject_entityManager");
        }
    }
}
