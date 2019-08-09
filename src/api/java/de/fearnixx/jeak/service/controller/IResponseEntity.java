package de.fearnixx.jeak.service.controller;

import java.util.Map;

public interface IResponseEntity<T> {

    void addHeader(String header, String value);

    void removeHeader(String header);

    Map<String, String> getHeaders();

    void setResponseEntity(T responseEntity);

    T getResponseEntity();
}
