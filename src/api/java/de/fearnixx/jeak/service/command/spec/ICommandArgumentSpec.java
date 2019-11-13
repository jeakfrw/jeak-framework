package de.fearnixx.jeak.service.command.spec;

public interface ICommandArgumentSpec extends IEvaluatedCriterion<ICommandArgumentSpec> {

    String getName();

    String getShorthand();
}
