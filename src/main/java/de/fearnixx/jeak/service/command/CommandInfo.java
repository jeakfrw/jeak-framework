package de.fearnixx.jeak.service.command;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandInfo {

    private final List<String> parameters = new LinkedList<>();
    private final Map<String, String> arguments = new LinkedHashMap<>();
    private final List<String> errorMessages = new LinkedList<>();

    public boolean isParameterized() {
        return !parameters.isEmpty();
    }

    public boolean isArgumentized() {
        return !arguments.isEmpty();
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
