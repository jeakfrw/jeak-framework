package de.fearnixx.jeak.service.command.spec;

public interface ICommandArgumentSpec extends EvaluatedSpec<ICommandArgumentSpec> {

    String getName();

    String getShorthand();
}
