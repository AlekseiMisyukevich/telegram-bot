package com.telegram;

import com.telegram.model.Round;

import javax.ws.rs.HEAD;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MessageBuilder {

    private StringBuilder builder;
    private final String LINE_SEPARATOR = System.lineSeparator();
    private Round round;
    final ZoneId zone;

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
        builder.append("Server time : " + time.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Round starts at: ");
        builder.append( getStartOfRound().withNano(0).toString().replace("T", " ") );
        return builder.toString();
    }

    public String getInvitationMsg() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("Registration's started.");
        builder.append("Server time : " + time.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Round start at: ");
        builder.append( getStartOfRound().withNano(0).toString().replace("T", " ") );
        builder.append(LINE_SEPARATOR);
        builder.append("Click button below to drop your name.");
        return builder.toString();
    }

    public String onRegistrationEnded() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("Round has already started.");
        builder.append(LINE_SEPARATOR);
        builder.append("Server time : " + time.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Next registration begins at: ");
        builder.append(getStartOfRound().minusMinutes(30).withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        return builder.toString();
    }

    public String getRoundStatus() {
        LocalDateTime time = getStartOfRound();
        LocalDateTime now = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("Server time: ");
        builder.append(now.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Round starts at: ");
        builder.append(time.withNano(0).toString().replace("T", " "));
        builder.append(LINE_SEPARATOR);
        builder.append("You will be notified to register at: ");
        builder.append(time.withNano(0).minusMinutes(30).toString().replace("T", " "));
        return builder.toString();
    }

    public String alreadyRegistered() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("You have already registed");
        builder.append(LINE_SEPARATOR);
        builder.append("Server time : " + time.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Round starts at: ");
        builder.append( getStartOfRound().withNano(0).toString().replace("T", " ") );
        return builder.toString();
    }

    private LocalDateTime getStartOfRound() {
        switch (round.getIteration()) {
            case 1: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(00).withMinute(00).withSecond(00).plusDays(1);
            }

            case 2: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(2).withMinute(00).withSecond(00);
            }

            case 3: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(4).withMinute(00).withSecond(00);
            }

            case 4: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(6).withMinute(00).withSecond(00);
            }

            case 5: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(8).withMinute(00).withSecond(00);
            }

            case 6: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(10).withMinute(00).withSecond(00);
            }

            case 7: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(12).withMinute(00).withSecond(00);
            }

            case 8: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(14).withMinute(00).withSecond(00);
            }


            case 9: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(16).withMinute(00).withSecond(00);
            }


            case 10: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(18).withMinute(00).withSecond(00);
            }


            case 11: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(20).withMinute(00).withSecond(00);
            }

            case 12: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(22).withMinute(00).withSecond(00);
            }
        }

        return LocalDateTime.now();
    }

    public MessageBuilder() {
        this.round = new Round();
        this.zone = ZoneId.systemDefault();
    }

}
