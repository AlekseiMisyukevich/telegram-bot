package com.telegram.handler;

import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotifierHandler {

    private Map<String, SendMessage> map;

    public NotifierHandler() {
        this.map = new ConcurrentHashMap<>();
    }

    public void createNotification(Long id, String username) {
        SendMessage msg = new SendMessage().setChatId(id);
        map.put(username, msg);

    }

    public Map<String, SendMessage> getMap() {
        return map;
    }
}
