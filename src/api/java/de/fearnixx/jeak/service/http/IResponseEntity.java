package de.fearnixx.jeak.service.http;

import java.util.Map;

public interface IResponseEntity<T> {
    void addHeader(String header, String value);
    void removeHeader(String header);
    Map<String, String> getHeaders();
    void setEntity(T entity);
    T getEntity();
    void setStatus(int status);
    int getStatus();
}
