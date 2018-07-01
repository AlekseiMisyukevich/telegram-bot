package com.telegram;

import com.telegram.model.Round;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class MessageBuilder {

    private StringBuilder builder;
    private final String LINE_SEPARATOR = System.lineSeparator();
    private Round round;
    final ZoneId zone;

    public String greeting() {
        builder = new StringBuilder();
        builder.append("Greetings...");
        builder.append(LINE_SEPARATOR);
        builder.append("First you have to provide instagram nickname. If telegram and instagram nicknames match click the button below.\n" +
                "Otherwise reply this message with your instagram nickname without @ sing.");
        return builder.toString();
    }

    public String getBuiltinCommands () {
        builder = new StringBuilder();
        builder.append("List of builtin commands:");
        builder.append(LINE_SEPARATOR);
        builder.append("/round - check round status and your engagment ");
        builder.append(LINE_SEPARATOR);
        builder.append("/whoami - let me introduce myself ");
        return builder.toString();
    }

    public String onRegistrationMessage() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("You have registered");
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
        builder.append(LINE_SEPARATOR);
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

    public String onInlineButtonSend() {
        builder = new StringBuilder();
        builder.append("Click /round to drop your name.");
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

    public String onRoundEnded() {
        builder = new StringBuilder();
        builder.append("Round has ended");
        builder.append(LINE_SEPARATOR);
        builder.append("To check next engagement round type /round");
        return builder.toString();
    }

    public String alreadyRegistered() {
        LocalDateTime time = LocalDateTime.now();
        builder = new StringBuilder();
        builder.append("You have already registered");
        builder.append(LINE_SEPARATOR);
        builder.append("Server time : " + time.withNano(0).toString().replace("T", " "));
        builder.append(" ");
        builder.append(zone.toString());
        builder.append(LINE_SEPARATOR);
        builder.append("Round starts at: ");
        builder.append( getStartOfRound().withNano(0).toString().replace("T", " ") );
        return builder.toString();
    }

    public String about() {
        return new String("Hey there, welcome to Doublegram.\n" +
                "I was designed to help you grow on Instagram!\n" +
                "Join upcoming round so others users can like, comment your content and they expect from you the same.\n" +
                "Use /round command to check round status."
        );
    }

    private LocalDateTime getStartOfRound() {
        switch (round.getIteration()) {
            case 1: {
                final LocalDateTime time = LocalDateTime.now();
                return time.withHour(0).withMinute(0).withSecond(0).plusDays(1);
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

    public MessageBuilder(Round round) {
        this.zone = ZoneId.systemDefault();
        this.round = round;
    }

}
