package de.fearnixx.jeak.teamspeak.query.api;

import de.fearnixx.jeak.event.query.RawQueryEvent;

import java.util.function.Consumer;

public interface ITSParser {

    void setOnGreetingCallback(Consumer<Boolean> greetingCompletedConsumer);

    void setOnAnswerCallback(Consumer<RawQueryEvent.Message.Answer> answerConsumer);

    void setOnNotificationCallback(Consumer<RawQueryEvent.Message.Notification> notificationConsumer);

    void parseLine(String line) throws QuerySyntaxException;
}
