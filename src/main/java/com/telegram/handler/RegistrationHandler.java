package com.telegram.handler;

import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationHandler {

    private ArrayList<SendMessage> messages;

    public RegistrationHandler() {
        this.messages = new ArrayList<>();
    }

    public void createNotification(Long id, String message) {
        SendMessage msg = new SendMessage().setChatId(id).setText(message);
        messages.add(msg);
    }

    public ArrayList<SendMessage> getMessages() {
        return messages;
    }

}
