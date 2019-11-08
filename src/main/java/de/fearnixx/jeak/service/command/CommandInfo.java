package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.service.command.spec.ICommandInfo;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandInfo implements ICommandInfo {

    private final List<String> parameters = new LinkedList<>();
    private final Map<String, String> arguments = new LinkedHashMap<>();
    private final List<String> errorMessages = new LinkedList<>();

    @Override
    public boolean isParameterized() {
        return !parameters.isEmpty();
    }

    @Override
    public boolean isArgumentized() {
        return !arguments.isEmpty();
    }

    @Override
    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, String> getArguments() {
        return arguments;
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
