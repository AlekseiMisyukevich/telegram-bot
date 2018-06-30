package com.telegram.handler;

import com.telegram.MessageBuilder;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationHandler {

    private Map<String, SendMessage> map;

    public RegistrationHandler() {
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
