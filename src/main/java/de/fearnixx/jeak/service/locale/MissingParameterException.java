package de.fearnixx.jeak.service.locale;

public class MissingParameterException extends Exception {

    private final String parameterName;

    public MissingParameterException(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
