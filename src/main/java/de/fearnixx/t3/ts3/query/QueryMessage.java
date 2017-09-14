package de.fearnixx.t3.ts3.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryMessage implements IQueryMessage {

    private List<QueryMessageObject> objects = new ArrayList<>();
    private QueryMessageObject.Error error;

    @Override
    public List<IQueryMessageObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    /* Return the real reference for internal use */
    protected List<QueryMessageObject> getRawObjects() {
        return objects;
    }

    /* Set the error response */
    protected void setError(QueryMessageObject.Error error) {
        this.error = error;
    }

    @Override
    public QueryMessageObject.Error getError() {
        return error;
    }
}
