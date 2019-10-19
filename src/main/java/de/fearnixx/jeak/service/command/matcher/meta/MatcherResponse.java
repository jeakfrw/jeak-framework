package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;

public class MatcherResponse implements IMatcherResponse {

    public static final IMatcherResponse SUCCESS = new MatcherResponse();
    private final MatcherResponseType type;
    private final String noticeMessage;
    private final int failedAtIndex;
    private final String failureMessage;

    public MatcherResponse() {
        this.type = MatcherResponseType.SUCCESS;
        this.noticeMessage = null;
        this.failedAtIndex = -1;
        this.failureMessage = null;
    }

    public MatcherResponse(MatcherResponseType type, int failedAtIndex, String failureMessage) {
        this.type = type;
        this.noticeMessage = null;
        this.failedAtIndex = failedAtIndex;
        this.failureMessage = failureMessage;
    }

    public MatcherResponse(String noticeMessage) {
        this.type = MatcherResponseType.NOTICE;
        this.noticeMessage = noticeMessage;
        this.failedAtIndex = -1;
        this.failureMessage = null;
    }

    @Override
    public MatcherResponseType getResponseType() {
        return type;
    }

    @Override
    public String getNoticeMessage() {
        return noticeMessage;
    }

    @Override
    public int getFailedAtIndex() {
        return failedAtIndex;
    }

    @Override
    public String getFailureMessage() {
        return failureMessage;
    }
}
