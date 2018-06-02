package com.telegram.handler;

import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.*;

public class NotifierHandler {

    private StringBuilder builder;
    private Round round;
    private final String LINE_SEPARATOR = System.getProperty("line.separator");
    private ArrayList<SendMessage> messages;

    public NotifierHandler( ) {
        this.messages = new ArrayList<>();
    }

    public void createNotification(Long id) {
        SendMessage msg = new SendMessage().setChatId(id).setText(buildMsg());
        messages.add(msg);
    }

    private String buildMsg () {
        builder = new StringBuilder();
        builder.append("Registration's started.");
        builder.append(LINE_SEPARATOR);
        builder.append("Click button below to drop your name.");

        return builder.toString();
    }

    public Iterator<SendMessage> getIterator() {
        return messages.iterator();
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }
}
