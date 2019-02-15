package de.fearnixx.jeak.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FieldSearch {

    private static final Logger logger = LoggerFactory.getLogger(FieldSearch.class);

    private static final Map<Class<?>, List<Field>> resultCache = new HashMap<>();

    public List<Field> getAnnotatedFields(Class<?> cls) {
        final LinkedList<Field> results = new LinkedList<>();
        aggregateFields(cls, results);
        return results;
    }

    private void aggregateFields(Class<?> cls, List<Field> list) {
        List<Field> cachedFields = fromCache(cls);
        if (cachedFields != null) {
            list.addAll(cachedFields);
            return;
        }

        for (Field field : cls.getDeclaredFields()) {
            Inject annotation = field.getAnnotation(Inject.class);

            if (annotation != null) {
                if (logger.isDebugEnabled()) {
                    final String fieldName = field.toGenericString();
                    logger.debug("Discovered field: {}", fieldName);
                }
                list.add(field);
            }
        }

        final Class<?> superClass = cls.getSuperclass();
        if (superClass != null && !superClass.isSynthetic()) {
            aggregateFields(superClass, list);
        }
        toCache(cls, new LinkedList<>(list));
    }

    private List<Field> fromCache(Class<?> cls) {
        synchronized (resultCache) {
            return resultCache.getOrDefault(cls, null);
        }
    }

    private void toCache(Class<?> cls, LinkedList<Field> fields) {
        synchronized (resultCache) {
            resultCache.put(cls, fields);
        }
    }
}
