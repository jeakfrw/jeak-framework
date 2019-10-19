package de.fearnixx.jeak.service.command.spec;

import java.util.List;
import java.util.Optional;

public interface ICommandSpec {

    String getCommand();

    List<String> getAliases();

    List<ICommandParamSpec> getParameters();

    List<ICommandArgumentSpec> getArguments();

    ICommandExecutor getExecutor();

    Optional<String> getRequiredPermission();

    int getRequiredPermissionValue();
}
