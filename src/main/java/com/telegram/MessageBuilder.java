package com.telegram;

import com.telegram.model.Round;

import java.time.LocalDateTime;

public class MessageBuilder {

    private StringBuilder builder;
    private final String LINE_SEPARATOR = System.lineSeparator();
    private Round round;

    public String greeting() {
        builder = new StringBuilder();
        builder.append("Bot is running.");
        builder.append(LINE_SEPARATOR);
        builder.append("Type /help to see builtin commands.");
        return builder.toString();
    }

    public String getBuiltinCommands () {
        builder = new StringBuilder();
        builder.append("List of builtin commands");
        builder.append(LINE_SEPARATOR);
        builder.append("/round - check round status and your engagment ");
        return builder.toString();
    }

    public String onRegistrationMessage() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("You have registed");
        builder.append(LINE_SEPARATOR);
        builder.append("Server time -> " + time);
        builder.append(LINE_SEPARATOR);
        builder.append("Round start at: ");
        builder.append( getStartOfRound() );
        return builder.toString();
    }

    public String getInvitationMsg() {
        builder = new StringBuilder();
        builder.append("Registration's started.");
        builder.append(LINE_SEPARATOR);
        builder.append("Click button below to drop your name.");
        return builder.toString();
    }

    public String onRegistrationEnded() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("Round has already started.");
        builder.append(LINE_SEPARATOR);
        builder.append("Server time -> " + time);
        builder.append(LINE_SEPARATOR);
        builder.append("Next registration begins at: ");
        builder.append(getStartOfRound().minusMinutes(30).toString());
        return builder.toString();
    }

    public String getRoundStatus() {
        final LocalDateTime time = getStartOfRound();
        final LocalDateTime now = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("Server time ");
        builder.append(now);
        builder.append(LINE_SEPARATOR);
        builder.append("Round starts at ");
        builder.append(time);
        builder.append(LINE_SEPARATOR);
        builder.append("You will be notified to register at ");
        builder.append(time.minusMinutes(30));
        return builder.toString();
    }

    private LocalDateTime getStartOfRound() {
        switch (round.getIteration()) {
            case 1: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(00).withMinute(00).withSecond(00);
            }

            case 2: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(2).withMinute(00).withSecond(00);
            }

            case 3: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(4).withMinute(00).withSecond(00);
            }

            case 4: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(6).withMinute(00).withSecond(00);
            }

            case 5: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(8).withMinute(00).withSecond(00);
            }

            case 6: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(10).withMinute(00).withSecond(00);
            }

            case 7: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(12).withMinute(00).withSecond(00);
            }

            case 8: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(14).withMinute(00).withSecond(00);
            }


            case 9: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(16).withMinute(00).withSecond(00);
            }


            case 10: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(18).withMinute(00).withSecond(00);
            }


            case 11: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(20).withMinute(00).withSecond(00);
            }

            case 12: {
                LocalDateTime time = LocalDateTime.now();
                return time.withHour(22).withMinute(00).withSecond(00);
            }

        }

        return LocalDateTime.now();
    }

    public MessageBuilder() {
        this.round = new Round();
    }

}
