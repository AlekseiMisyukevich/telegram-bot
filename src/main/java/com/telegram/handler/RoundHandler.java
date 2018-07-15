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

    public void addUser(Long id, String username) { chatAndUserIds.put(id, buildeURL(username)); }

    public boolean isUserRegistered(Long key) {
        return chatAndUserIds.containsKey(key);
    }

    public Iterator<SendMessage> buildMessageIterator() {
        ArrayList<SendMessage> messages = new ArrayList<>();
        String list = createUsernamesList();
        for (Map.Entry<Long, String> id : chatAndUserIds.entrySet()) {
            SendMessage newMessage = new SendMessage().setChatId(id.getKey()).setText(list)
                    .enableHtml(true).disableWebPagePreview();
            messages.add(newMessage);
        }
        return messages.iterator();
    }

    private String createUsernamesList() {
        StringBuilder builder = new StringBuilder();
        builder.append("Round lasts 1 hour. ");
        builder.append("Look up users on Instragram.");
        builder.append(LINE_SEPARATOR);
        builder.append("Subscribe, likes and comments thier content.");
        builder.append(LINE_SEPARATOR);
        builder.append("List of attendees:");
        builder.append(LINE_SEPARATOR);
        for (Map.Entry<Long, String> entry : chatAndUserIds.entrySet()) {
            builder.append(entry.getValue());
            builder.append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    private String buildeURL(String username) {
        StringBuilder builder = new StringBuilder();
        String href = String.format("<a href=\"https://www.instagram.com/%s/\">", username);
        builder.append( href );
        builder.append(username);
        builder.append("</a>");
        return builder.toString();
    }

    public HashMap<Long, String> getChatAndUserIds() {
        return chatAndUserIds;
    }
}
