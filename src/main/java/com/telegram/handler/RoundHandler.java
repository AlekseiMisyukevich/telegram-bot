package com.telegram.handler;

import com.telegram.model.Round;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.*;

public class RoundHandler {

    private HashMap<Long, String> chatAndUserIds;
    private Round round;
    private final String LINE_SEPARATOR = System.lineSeparator();

    public RoundHandler(Round round ) {
        this.chatAndUserIds = new HashMap<>();
        this.round = round;
    }

    public Round getRound() {
        return round;
    }

    public void addUser(Long id, String username) {
        chatAndUserIds.put(id, "@" + username);
    }

    public boolean isUserRegistered(Long key) {
        return chatAndUserIds.containsKey(key);
    }

    public Iterator<SendMessage> messageIterator() {
        ArrayList<SendMessage> messages = new ArrayList<>();
        String list = createUsernamesList();
        for (Map.Entry<Long, String> id : chatAndUserIds.entrySet()) {
            SendMessage newMessage = new SendMessage().setChatId(id.getKey()).setText(list);
            messages.add(newMessage);
        }
        return messages.iterator();
    }

    private String createUsernamesList() {
        StringBuilder builder = new StringBuilder();
        builder.append("Round lasts 1 hour.");
        builder.append(LINE_SEPARATOR);
        builder.append("Search users on Instagram.");
        builder.append(LINE_SEPARATOR);
        builder.append("Subscribe, leave likes and comments");
        builder.append(LINE_SEPARATOR);
        builder.append("List of attendees:\n");
        for (Map.Entry<Long, String> entry : chatAndUserIds.entrySet()) {
            builder.append(entry.getValue());
            builder.append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    public HashMap<Long, String> getChatAndUserIds() {
        return chatAndUserIds;
    }
}
