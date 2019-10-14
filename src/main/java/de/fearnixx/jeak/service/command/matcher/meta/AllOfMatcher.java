package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.CommandV2Context;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

import java.util.LinkedList;
import java.util.List;

public class AllOfMatcher implements MetaMatcher {

    private final List<IParameterMatcher<?>> parameters = new LinkedList<>();

    @Override
    public int tryMatch(CommandV2Context ctx) {

    }
}
