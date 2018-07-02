package com.telegram;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class RepleyKeyboardBuilder {

    private final String REG_FLAG = "0x494E";
    private final String ROUND_FLAG = "0x454E";

    public final InlineKeyboardMarkup getNameButton(String nickname) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton().setText(nickname).setCallbackData(REG_FLAG);
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

    public String getFLAG() {
        return REG_FLAG;
    }

    public String getCHAT_FLAG() { return ROUND_FLAG; }
}
