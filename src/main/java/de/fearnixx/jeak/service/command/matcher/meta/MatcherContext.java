package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

public class MatcherContext {

    private String failedAtPermission;
    private int failedAtPermissionValue;
    private IParameterMatcher failedAtMatcher;
    private int lastParameterPosition;

    public String getFailedAtPermission() {
        return failedAtPermission;
    }

    public void setFailedAtPermission(String failedAtPermission) {
        this.failedAtPermission = failedAtPermission;
    }

    public int getFailedAtPermissionValue() {
        return failedAtPermissionValue;
    }

    public void setFailedAtPermissionValue(int failedAtPermissionValue) {
        this.failedAtPermissionValue = failedAtPermissionValue;
    }

    public IParameterMatcher getFailedAtMatcher() {
        return failedAtMatcher;
    }

    public void setFailedAtMatcher(IParameterMatcher failedAtMatcher) {
        this.failedAtMatcher = failedAtMatcher;
    }

    public int getLastParameterPosition() {
        return lastParameterPosition;
    }

    public void setLastParameterPosition(int lastParameterPosition) {
        this.lastParameterPosition = lastParameterPosition;
    }
}
