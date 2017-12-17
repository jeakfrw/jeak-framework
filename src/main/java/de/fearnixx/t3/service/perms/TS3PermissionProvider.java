package de.fearnixx.t3.service.perms;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.fearnixx.t3.service.perms.catalogued.CataloguedTS3Permission;
import de.fearnixx.t3.service.perms.catalogued.TS3Permission;
import de.fearnixx.t3.service.perms.permission.IPermission;
import de.fearnixx.t3.service.perms.permission.IPermissionEntry;
import de.fearnixx.t3.service.perms.permission.PermSourceType;
import de.fearnixx.t3.ts3.query.IQueryConnection;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public abstract class TS3PermissionProvider implements IPermProvider {

    private static final Object classLock = new Object();
    private static Map<String, TS3Permission> cataloguedPerms;

    private IQueryConnection connection;

    private final Object lock = new Object();
    private final BiMap<String, Integer> permIDCache;

    public TS3PermissionProvider(IQueryConnection connection) {
        permIDCache = HashBiMap.create();

        synchronized (classLock) {
            // Scan for created TS3 permissions just due to laziness of manually listing them here
            // We only need to run the scan once per runtime.
            if (cataloguedPerms == null) {
                cataloguedPerms = new HashMap<>();

                ClassLoader loader = this.getClass().getClassLoader();
                ConfigurationBuilder builder = new ConfigurationBuilder();
                Reflections ref = new Reflections(builder);
                Set<Class<?>> results = ref.getTypesAnnotatedWith(CataloguedTS3Permission.class);
                Map<String, TS3Permission> fCataloguedPerms = cataloguedPerms;
                results.stream()
                       .filter(TS3Permission.class::isAssignableFrom)
                       .forEach(c -> {
                           try {
                               Object perm = c.newInstance();
                               fCataloguedPerms.put(
                                   ((TS3Permission) perm).getInternalSID(),
                                   ((TS3Permission) perm)
                               );
                           } catch (Exception e) {
                               throw new RuntimeException("Cannot create CataloguedTS3Permission!", e);
                           }
                       });
                cataloguedPerms = Collections.unmodifiableMap(cataloguedPerms);
            }
        }
    }

    @Override
    public Optional<IPermission> getPermission(String systemSID) {
        return Optional.ofNullable(cataloguedPerms.getOrDefault(systemSID, null));
    }

    @Override
    public Optional<IPermissionEntry> getEffectivePermission(String systemSID, Integer target, PermSourceType type) {
        List<IPermissionEntry> context = getPermissionContext(systemSID, target, type);
        if (context.size() > 0)
            return Optional.of(context.get(0));
        return Optional.empty();
    }


    public List<IPermissionEntry> getPermissionContext(String systemSID, Integer target, PermSourceType type) {
        synchronized (lock) {
            Integer systemID = permIDCache.getOrDefault(systemSID, null);
            if (systemID == null) {
                // TODO: Lazily blocking query numerical permission ID
            }
            return getPermissionContext(systemID, target, type);
        }
    }

    private List<IPermissionEntry> getPermissionContext(Integer systemID, Integer target, PermSourceType type) {
        if (systemID == null) return Collections.emptyList();

        List<IPermissionEntry> entries = new ArrayList<>();
        switch (type) {
            case SERVER_GROUP: ;
            case CLIENT: ;
            case CHANNEL_GROUP: ;
            case CHANNEL: ;
            default: return Collections.emptyList();
        }

    }
}
