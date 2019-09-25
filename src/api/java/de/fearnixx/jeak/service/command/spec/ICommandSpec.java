package de.fearnixx.jeak.service.command.spec;

import java.util.List;
import java.util.function.Consumer;

public interface ICommandSpec {

    String getCommand();

    List<String> getAliases();

    List<ICommandParamSpec> getParameters();

    List<ICommandArgumentSpec> getArguments();

    Consumer<Object> getExecutor();
}
