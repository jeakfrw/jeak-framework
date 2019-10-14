package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;

import java.util.Optional;

public class GroupParameterMatcher implements IParameterMatcher<IGroup> {

    @Inject
    private IPermissionService permService;

    @Override
    public Class<IGroup> getSupportedType() {
        return IGroup.class;
    }

    @Override
    public Optional<IGroup> tryMatch(String paramString) {
        return permService.getFrameworkProvider().findGroupByName(paramString);
    }
}
