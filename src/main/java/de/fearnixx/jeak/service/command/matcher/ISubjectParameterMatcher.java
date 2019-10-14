package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.Optional;
import java.util.UUID;

public class ISubjectParameterMatcher implements IParameterMatcher<ISubject> {

    @Inject
    private IPermissionService permissionService;

    @Override
    public Class<ISubject> getSupportedType() {
        return ISubject.class;
    }

    @Override
    public Optional<ISubject> tryMatch(String paramString) {
        return permissionService.getFrameworkProvider().getSubject(UUID.fromString(paramString));
    }
}
