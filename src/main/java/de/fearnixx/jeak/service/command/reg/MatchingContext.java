package de.fearnixx.jeak.service.command.reg;

import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MatchingContext implements IMatchingContext {

    private final String paramName;
    private final String argShorthand;
    private final List<MatchingContext> children = new LinkedList<>();
    private final IParameterMatcher<?> matcher;

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

    public void addChild(MatchingContext child) {
        children.add(child);
    }
}
