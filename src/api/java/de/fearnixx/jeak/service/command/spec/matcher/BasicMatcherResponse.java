package de.fearnixx.jeak.service.command.spec.matcher;

public class BasicMatcherResponse implements IMatcherResponse {

    public static final IMatcherResponse SUCCESS = new BasicMatcherResponse();
    private final MatcherResponseType type;
    private final String noticeMessage;
    private final int failedAtIndex;
    private final String failureMessage;

    public BasicMatcherResponse() {
        this.type = MatcherResponseType.SUCCESS;
        this.noticeMessage = null;
        this.failedAtIndex = -1;
        this.failureMessage = null;
    }

    public BasicMatcherResponse(MatcherResponseType type, int failedAtIndex, String failureMessage) {
        this.type = type;
        this.noticeMessage = null;
        this.failedAtIndex = failedAtIndex;
        this.failureMessage = failureMessage;
    }

    public BasicMatcherResponse(String noticeMessage) {
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
