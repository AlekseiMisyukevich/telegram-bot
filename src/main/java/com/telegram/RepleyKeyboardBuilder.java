package com.telegram;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class RepleyKeyboardBuilder {

    private final String REGISTRATION_FLAG = "reg";
    private final String ROUND_FLAG = "round";

    public final InlineKeyboardMarkup getNameButton(String username) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton().setText(username).setCallbackData(REGISTRATION_FLAG);
        row.add(button);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public final InlineKeyboardMarkup getEnganeButton() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton().setText("Click to engage").setCallbackData(ROUND_FLAG);
        row.add(button);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public String getREG_FLAG() {
        return REGISTRATION_FLAG;
    }

    public String getROUND_FLAG() {
        return ROUND_FLAG;
    }
}
