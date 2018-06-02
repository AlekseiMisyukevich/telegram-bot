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
        builder.append("/round - check round status ");
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

    public String getRoundStatus(int iteration) {
        builder = new StringBuilder();
        return builder.toString();
    }

    private LocalDateTime getStartOfRound() {
        LocalDateTime time = null;
        switch (round.getIteration()) {
            case 1: {
                time = LocalDateTime.now();
                time.withHour(00).withMinute(00);
                break;
            }

            case 2: {
                time = LocalDateTime.now();
                time.withHour(3).withMinute(00);
                break;
            }

            case 3: {
                time = LocalDateTime.now();
                time.withHour(6).withMinute(00);
                break;
            }

            case 4: {
                time = LocalDateTime.now();
                time.withHour(9).withMinute(00);
                break;
            }

            case 5: {
                time = LocalDateTime.now();
                time.withHour(12).withMinute(00);
                break;
            }
            case 6: {
                time = LocalDateTime.now();
                time.withHour(15).withMinute(00);
                break;
            }
            case 7: {
                time = LocalDateTime.now();
                time.withHour(18).withMinute(00);
                break;
            }
            case 8: {
                time = LocalDateTime.now();
                time.withHour(21).withMinute(00);
                break;
            }
        }
        return time;
    }

    public MessageBuilder() {
        this.round = round;
    }

}
