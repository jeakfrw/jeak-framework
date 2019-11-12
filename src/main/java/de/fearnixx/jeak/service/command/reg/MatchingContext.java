package de.fearnixx.jeak.service.command.reg;

import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

import java.util.*;

public class MatchingContext implements IMatchingContext {

    private final String paramName;
    private final String argShorthand;
    private final List<MatchingContext> children = new LinkedList<>();
    private final IParameterMatcher<?> matcher;
    private ICommandSpec commandSpec;

    public MatchingContext(String argName, String argShorthand, IParameterMatcher<?> useMatcher) {
        this.paramName = argName;
        this.argShorthand = argShorthand;
        this.matcher = useMatcher;
    }

    public MatchingContext(String paramName, IParameterMatcher<?> useMatcher) {
        this.paramName = paramName;
        this.argShorthand = null;
        this.matcher = useMatcher;
    }

    @Override
    public boolean isParameter() {
        return argShorthand == null;
    }

    @Override
    public boolean isArgument() {
        return argShorthand != null;
    }

    @Override
    public String getArgumentOrParamName() {
        return paramName;
    }

    @Override
    public String getArgShorthand() {
        return argShorthand;
    }

    @Override
    public List<IMatchingContext> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public IParameterMatcher<?> getMatcher() {
        return matcher;
    }

    @Override
    public ICommandSpec getCommandSpec() {
        return commandSpec;
    }

    public void setCommandSpec(ICommandSpec commandSpec) {
        this.commandSpec = commandSpec;
        if (!children.isEmpty()) {
            children.forEach(c -> c.setCommandSpec(commandSpec));
        }
    }

    public void addChild(MatchingContext child) {
        children.add(child);
    }
}
