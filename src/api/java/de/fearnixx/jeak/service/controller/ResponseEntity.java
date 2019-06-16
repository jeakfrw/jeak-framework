package de.fearnixx.jeak.service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResponseEntity<T> implements IResponseEntity<T> {
    private Map<String, String> headers;
    private T responseEntity;

    public ResponseEntity(T responseEntity) {
        this.headers = new HashMap<>();
        this.responseEntity = responseEntity;
    }

    @Override
    public void addHeader(String header, String value) {
        Objects.requireNonNull(header);
        Objects.requireNonNull(value);
        this.headers.put(header, value);
    }

    @Override
    public void removeHeader(String header) {
        Objects.requireNonNull(header);
        this.headers.remove(header);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }

    @Override
    public T getResponseEntity() {
        return responseEntity;
    }
}
