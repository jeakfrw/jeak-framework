package de.fearnixx.jeak.service.command.spec;

import java.util.List;
import java.util.Map;

public interface ICommandInfo {
    boolean isParameterized();

    boolean isArgumentized();

    List<String> getParameters();

    Map<String, String> getArguments();

    List<String> getErrorMessages();
}
