package de.fearnixx.jeak.service.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResponseEntity<T> implements IResponseEntity<T> {
    private Map<String, String> headers;
    private T entity;
    private int httpStatus;

    public ResponseEntity(T entity) {
        this.headers = new HashMap<>();
        this.entity = entity;
    }

    private ResponseEntity(Map<String, String> headers, T entity, int httpStatus) {
        this.headers = headers;
        this.entity = entity;
        this.httpStatus = httpStatus;
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
    public void setEntity(T entity) {
        this.entity = entity;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public void setStatus(int status) {
        this.httpStatus = status;
    }

    @Override
    public int getStatus() {
        return httpStatus;
    }

    public static class Builder<T> {
        private Map<String, String> headers;
        private T responseEntity;
        private int httpStatus;

        public Builder(T responseEntity) {
            this.responseEntity = responseEntity;
            this.headers = new HashMap<>();
        }

        public Builder() {
            this.responseEntity = null;
            this.headers = new HashMap<>();
        }

        public Builder<T> withHeader(String fieldName, String value) {
            headers.put(fieldName, value);
            return this;
        }

        public Builder<T> withHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder<T> withStatus(int httStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public ResponseEntity<T> build() {
            return new ResponseEntity<>(headers, responseEntity, httpStatus);
        }
    }
}
