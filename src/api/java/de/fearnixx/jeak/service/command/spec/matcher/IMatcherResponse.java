package de.fearnixx.jeak.service.command.spec.matcher;

public interface IMatcherResponse {

    MatcherResponseType getResponseType();

    String getNoticeMessage();

    String getFailureMessage();

    int getFailedAtIndex();
}
